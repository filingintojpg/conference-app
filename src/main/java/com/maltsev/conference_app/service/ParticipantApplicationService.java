package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import com.maltsev.conference_app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ParticipantApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DirectionRepository directionRepository;

    public ParticipantApplicationService(ApplicationRepository applicationRepository,
                                         UserRepository userRepository,
                                         DirectionRepository directionRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.directionRepository = directionRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(Long participantId) {
        return applicationRepository.findByParticipantIdOrderByIdDesc(participantId).stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse create(Long participantId, CreateApplicationRequest request) {
        User participant = userRepository.findById(participantId).orElseThrow(() -> new NotFoundException("User not found: " + participantId));
        Direction direction = directionRepository.findById(request.directionId()).orElseThrow(() -> new NotFoundException("Direction not found: " + request.directionId()));

        checkDeadline(direction, OffsetDateTime.now());

        Application application = applicationRepository.save(new Application(
                participant,
                direction,
                request.title(),
                request.abstractText(),
                request.content()
        ));

        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse update(Long id, Long participantId, UpdateApplicationRequest request) {
        Application application = getApplication(id);
        checkOwner(application, participantId);
        checkNotWithdrawn(application);
        checkNotReviewed(application);
        checkDeadline(application.getDirection(), OffsetDateTime.now());
        application.edit(request.title(), request.abstractText(), request.content());
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse withdraw(Long id, Long participantId) {
        Application application = getApplication(id);
        checkOwner(application, participantId);
        checkNotWithdrawn(application);
        checkDeadline(application.getDirection(), OffsetDateTime.now());
        application.withdraw();
        return ApplicationResponse.from(application);
    }

    private Application getApplication(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
    }

    private void checkOwner(Application application, Long participantId) {
        if (!application.getParticipant().getId().equals(participantId)) {
            throw new ForbiddenException("Application belongs to another participant");
        }
    }

    private void checkNotWithdrawn(Application application) {
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ConflictException("Application is already withdrawn");
        }
    }

    private void checkNotReviewed(Application application) {
        if (application.isReviewed()) {
            throw new ConflictException("Application has already been reviewed and cannot be edited");
        }
    }

    private void checkDeadline(Direction direction, OffsetDateTime now) {
        if (!now.isBefore(direction.getDeadline())) {
            throw new ConflictException("T1 has passed");
        }
    }
}
