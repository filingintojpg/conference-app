package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.security.RequiresRole;
import com.maltsev.conference_app.security.UserHeader;
import com.maltsev.conference_app.service.ExpertApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/expert/applications")
@RequiresRole(Role.EXPERT)
public class ExpertApplicationController {
    private final ExpertApplicationService service;

    public ExpertApplicationController(ExpertApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpertApplicationResponse> list(@RequestHeader(UserHeader.NAME) Long expertId) {
        return service.list(expertId);
    }
}
