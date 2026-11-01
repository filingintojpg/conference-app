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
        return find(directionId, status, reviewStatus).stream().map(ApplicationResponse::from).toList();
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
        checkDirectionOwner(application.getDirection(), organizerId);
        return ApplicationResponse.from(application);
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
        return application;
    }

    private void checkDirectionOwner(Direction direction, Long organizerId) {
        if (!direction.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("Direction belongs to another organizer");
        }
    }
}
