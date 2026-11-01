package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpertApplicationService {
    private final ApplicationRepository applicationRepository;
    private final DirectionRepository directionRepository;

    public ExpertApplicationService(ApplicationRepository applicationRepository,
                                    DirectionRepository directionRepository) {
        this.applicationRepository = applicationRepository;
        this.directionRepository = directionRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpertApplicationResponse> list(Long directionId) {
        List<Application> applications;
        if (directionId == null) {
            applications = applicationRepository.findByReviewStatusAndStatusNotOrderById(
                    ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN);
        } else {
            if (!directionRepository.existsById(directionId)) {
                throw new NotFoundException("Direction not found: " + directionId);
            }
            applications = applicationRepository.findByDirectionIdAndReviewStatusAndStatusNotOrderById(
                    directionId, ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN);
        }
        return applications.stream().map(ExpertApplicationResponse::from).toList();
    }
}
