package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.DirectionResponse;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DirectionService {
    private final DirectionRepository directionRepository;

    public DirectionService(DirectionRepository directionRepository) {
        this.directionRepository = directionRepository;
    }

    @Transactional(readOnly = true)
    public List<DirectionResponse> list() {
        return directionRepository.findAll(Sort.by("id")).stream().map(DirectionResponse::from).toList();
    }
}
