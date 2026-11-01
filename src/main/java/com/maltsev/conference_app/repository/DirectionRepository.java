package com.maltsev.conference_app.repository;

import com.maltsev.conference_app.model.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
    Optional<Direction> findByExpertId(Long expertId);
}
