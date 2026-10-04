package com.maltsev.conference_app.repository;

import com.maltsev.conference_app.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
