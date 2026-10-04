package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.Participant;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import com.maltsev.conference_app.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ParticipantRepository participantRepository;
    private final DirectionRepository directionRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ParticipantRepository participantRepository,
                              DirectionRepository directionRepository) {
        this.applicationRepository = applicationRepository;
        this.participantRepository = participantRepository;
        this.directionRepository = directionRepository;
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request) {
        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + request.participantId()));
        Direction direction = directionRepository.findById(request.directionId())
                .orElseThrow(() -> new IllegalArgumentException("Direction not found: " + request.directionId()));

        OffsetDateTime now = OffsetDateTime.now();
        checkDeadline(direction, now);

        Application application = applicationRepository.save(new Application(
                participant,
                direction,
                request.title(),
                request.abstractText(),
                request.content(),
                now
        ));

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse update(Long id, UpdateApplicationRequest request) {
        Application application = getApplication(id);
        checkCanChange(application);
        application.edit(request.title(), request.abstractText(), request.content(), OffsetDateTime.now());
        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse withdraw(Long id) {
        Application application = getApplication(id);
        checkCanChange(application);
        application.withdraw(OffsetDateTime.now());
        return toResponse(application);
    }

    private Application getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    private void checkCanChange(Application application) {
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Application is already withdrawn");
        }
        checkDeadline(application.getDirection(), OffsetDateTime.now());
    }

    private void checkDeadline(Direction direction, OffsetDateTime now) {
        if (!now.isBefore(direction.getEditDeadline())) {
            throw new IllegalStateException("T1 has passed");
        }
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getParticipant().getId(),
                application.getDirection().getId(),
                application.getTitle(),
                application.getAbstractText(),
                application.getContent(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                application.getWithdrawnAt()
        );
    }
}
