package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private static final String PARTICIPANT_HEADER = "X-Participant-Id";

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@RequestHeader(PARTICIPANT_HEADER) Long participantId,
                                      @Valid @RequestBody CreateApplicationRequest request) {
        return service.create(participantId, request);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id,
                                      @RequestHeader(PARTICIPANT_HEADER) Long participantId,
                                      @Valid @RequestBody UpdateApplicationRequest request) {
        return service.update(id, participantId, request);
    }

    @PostMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@PathVariable Long id,
                                        @RequestHeader(PARTICIPANT_HEADER) Long participantId) {
        return service.withdraw(id, participantId);
    }
}
