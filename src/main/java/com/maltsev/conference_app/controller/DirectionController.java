package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.DirectionResponse;
import com.maltsev.conference_app.service.DirectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/directions")
public class DirectionController {
    private final DirectionService service;

    public DirectionController(DirectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<DirectionResponse> list() {
        return service.list();
    }
}
