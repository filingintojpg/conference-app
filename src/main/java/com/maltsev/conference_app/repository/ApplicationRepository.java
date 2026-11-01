package com.maltsev.conference_app.repository;

import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByDirectionIdOrderById(Long directionId);

    List<Application> findByDirectionIdAndStatusOrderById(Long directionId, ApplicationStatus status);

    List<Application> findByDirectionIdAndReviewStatusOrderById(Long directionId, ReviewStatus reviewStatus);

    List<Application> findByDirectionIdAndStatusAndReviewStatusOrderById(Long directionId,
                                                                         ApplicationStatus status,
                                                                         ReviewStatus reviewStatus);

    List<Application> findByDirectionIdAndReviewStatusAndStatusNotOrderById(Long directionId,
                                                                            ReviewStatus reviewStatus,
                                                                            ApplicationStatus status);

    List<Application> findByParticipantIdOrderByIdDesc(Long participantId);
}
