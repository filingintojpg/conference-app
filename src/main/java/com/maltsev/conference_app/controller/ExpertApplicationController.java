package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.security.AuthUser;
import com.maltsev.conference_app.service.ExpertApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/expert/applications")
public class ExpertApplicationController {
    private final ExpertApplicationService service;

    public ExpertApplicationController(ExpertApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpertApplicationResponse> list(Principal principal) {
        return service.list(AuthUser.id(principal));
    }
}
