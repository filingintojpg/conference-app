package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.OrganizerApplicationResponse;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
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
    public List<OrganizerApplicationResponse> list(Long organizerId, Long directionId, ApplicationStatus status) {
        Direction direction = directionRepository.findById(directionId).orElseThrow(() -> new NotFoundException("Direction not found: " + directionId));
        checkDirectionOwner(direction, organizerId);
        return applicationRepository.findByDirectionIdAndStatusOrderById(directionId, status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrganizerApplicationResponse get(Long id, Long organizerId) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Application not found: " + id));
        checkDirectionOwner(application.getDirection(), organizerId);
        return toResponse(application);
    }

    private void checkDirectionOwner(Direction direction, Long organizerId) {
        if (!direction.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("Direction belongs to another organizer");
        }
    }

    private OrganizerApplicationResponse toResponse(Application application) {
        return new OrganizerApplicationResponse(
                application.getId(),
                application.getDirection().getId(),
                application.getDirection().getName(),
                application.getParticipant().getId(),
                application.getParticipant().getFullName(),
                application.getParticipant().getEmail(),
                application.getTitle(),
                application.getAbstractText(),
                application.getContent(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
