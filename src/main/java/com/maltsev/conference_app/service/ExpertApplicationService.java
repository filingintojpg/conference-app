package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
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
    public List<ExpertApplicationResponse> list(Long expertId) {
        Direction direction = directionRepository.findByExpertId(expertId)
                .orElseThrow(() -> new ForbiddenException("Expert is not assigned to a direction"));
        return applicationRepository.findByDirectionIdAndReviewStatusAndStatusNotOrderById(
                        direction.getId(), ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN)
                .stream().map(ExpertApplicationResponse::from).toList();
    }
}
