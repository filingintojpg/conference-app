package com.maltsev.conference_app.repository;

import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByDirectionIdAndStatusOrderById(Long directionId, ApplicationStatus status);
}
