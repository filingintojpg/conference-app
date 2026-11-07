package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/applications")
public class ParticipantApplicationController {
    private final com.maltsev.conference_app.service.ParticipantApplicationService service;

    public ParticipantApplicationController(com.maltsev.conference_app.service.ParticipantApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationResponse> list(Principal principal) {
        return service.list(AuthUser.id(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(Principal principal,
                                      @Valid @RequestBody CreateApplicationRequest request) {
        return service.create(AuthUser.id(principal), request);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id,
                                      Principal principal,
                                      @Valid @RequestBody UpdateApplicationRequest request) {
        return service.update(id, AuthUser.id(principal), request);
    }

    @PostMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@PathVariable Long id,
                                        Principal principal) {
        return service.withdraw(id, AuthUser.id(principal));
    }
}
