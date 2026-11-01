package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizerApplicationService {
    private final ApplicationRepository applicationRepository;
    private final DirectionRepository directionRepository;

    public OrganizerApplicationService(ApplicationRepository applicationRepository,
                                       DirectionRepository directionRepository) {
        this.applicationRepository = applicationRepository;
        this.directionRepository = directionRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(Long organizerId, Long directionId,
                                          ApplicationStatus status, ReviewStatus reviewStatus) {
        Direction direction = directionRepository.findById(directionId).orElseThrow(() -> new NotFoundException("Direction not found: " + directionId));
        checkDirectionOwner(direction, organizerId);

        List<Application> result = new ArrayList<>(findIncomingTransfers(directionId, status, reviewStatus));
        result.addAll(find(directionId, status, reviewStatus));
        return result.stream().map(ApplicationResponse::from).toList();
    }

    private List<Application> findIncomingTransfers(Long directionId, ApplicationStatus status, ReviewStatus reviewStatus) {
        return applicationRepository.findByTransferToIdOrderById(directionId).stream()
                .filter(application -> status == null || application.getStatus() == status)
                .filter(application -> reviewStatus == null || application.getReviewStatus() == reviewStatus)
                .toList();
    }

    private List<Application> find(Long directionId, ApplicationStatus status, ReviewStatus reviewStatus) {
        if (status != null && reviewStatus != null) {
            return applicationRepository.findByDirectionIdAndStatusAndReviewStatusOrderById(directionId, status, reviewStatus);
        }
        if (status != null) {
            return applicationRepository.findByDirectionIdAndStatusOrderById(directionId, status);
        }
        if (reviewStatus != null) {
            return applicationRepository.findByDirectionIdAndReviewStatusOrderById(directionId, reviewStatus);
        }
        return applicationRepository.findByDirectionIdOrderById(directionId);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(Long id, Long organizerId) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
        if (!isTransferTargetOwner(application, organizerId)) {
            checkDirectionOwner(application.getDirection(), organizerId);
        }
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse transfer(Long id, Long organizerId, Long targetDirectionId) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
        checkDirectionOwner(application.getDirection(), organizerId);
        Direction target = directionRepository.findById(targetDirectionId).orElseThrow(() -> new NotFoundException("Direction not found: " + targetDirectionId));
        if (target.getId().equals(application.getDirection().getId())) {
            throw new ConflictException("Application already belongs to this direction");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Application is withdrawn and cannot be transferred");
        }
        if (application.isReviewed()) {
            throw new ConflictException("Application has already been reviewed and cannot be transferred");
        }
        if (application.isTransferPending()) {
            throw new ConflictException("Application is already waiting for a transfer decision");
        }
        application.requestTransfer(target);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse acceptTransfer(Long id, Long organizerId) {
        Application application = getWithPendingTransfer(id, organizerId);
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Application is withdrawn and cannot be accepted");
        }
        application.acceptTransfer();
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse rejectTransfer(Long id, Long organizerId) {
        Application application = getWithPendingTransfer(id, organizerId);
        application.rejectTransfer();
        return ApplicationResponse.from(application);
    }

    private Application getWithPendingTransfer(Long id, Long organizerId) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
        if (!application.isTransferPending()) {
            throw new ConflictException("Application has no pending transfer");
        }
        checkDirectionOwner(application.getTransferTo(), organizerId);
        return application;
    }

    private boolean isTransferTargetOwner(Application application, Long organizerId) {
        return application.isTransferPending()
                && application.getTransferTo().getOrganizer().getId().equals(organizerId);
    }

    @Transactional
    public ApplicationResponse approve(Long id, Long organizerId) {
        Application application = getForReview(id, organizerId);
        application.approve();
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse reject(Long id, Long organizerId) {
        Application application = getForReview(id, organizerId);
        application.reject();
        return ApplicationResponse.from(application);
    }

    private Application getForReview(Long id, Long organizerId) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
        checkDirectionOwner(application.getDirection(), organizerId);
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Application is withdrawn and cannot be reviewed");
        }
        if (application.isReviewed()) {
            throw new ConflictException("Application has already been reviewed");
        }
        if (application.isTransferPending()) {
            throw new ConflictException("Application is waiting for a transfer decision and cannot be reviewed");
        }
        return application;
    }

    private void checkDirectionOwner(Direction direction, Long organizerId) {
        if (!direction.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("Direction belongs to another organizer");
        }
    }
}
