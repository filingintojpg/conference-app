package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.security.RequiresRole;
import com.maltsev.conference_app.security.UserHeader;
import com.maltsev.conference_app.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@RequiresRole(Role.PARTICIPANT)
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@RequestHeader(UserHeader.NAME) Long participantId,
                                      @Valid @RequestBody CreateApplicationRequest request) {
        return service.create(participantId, request);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id,
                                      @RequestHeader(UserHeader.NAME) Long participantId,
                                      @Valid @RequestBody UpdateApplicationRequest request) {
        return service.update(id, participantId, request);
    }

    @PostMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@PathVariable Long id,
                                        @RequestHeader(UserHeader.NAME) Long participantId) {
        return service.withdraw(id, participantId);
    }
}
