package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.TransferApplicationRequest;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.security.AuthUser;
import com.maltsev.conference_app.service.OrganizerApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/organizer/applications")
public class OrganizerApplicationController {
    private final OrganizerApplicationService service;

    public OrganizerApplicationController(OrganizerApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationResponse> list(Principal principal,
                                                   @RequestParam Long directionId,
                                                   @RequestParam(required = false) ApplicationStatus status,
                                                   @RequestParam(required = false) ReviewStatus reviewStatus) {
        return service.list(AuthUser.id(principal), directionId, status, reviewStatus);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id,
                                            Principal principal) {
        return service.get(id, AuthUser.id(principal));
    }

    @PostMapping("/{id}/approve")
    public ApplicationResponse approve(@PathVariable Long id,
                                       Principal principal) {
        return service.approve(id, AuthUser.id(principal));
    }

    @PostMapping("/{id}/reject")
    public ApplicationResponse reject(@PathVariable Long id,
                                      Principal principal) {
        return service.reject(id, AuthUser.id(principal));
    }

    @PostMapping("/{id}/transfer")
    public ApplicationResponse transfer(@PathVariable Long id,
                                        Principal principal,
                                        @Valid @RequestBody TransferApplicationRequest request) {
        return service.transfer(id, AuthUser.id(principal), request.targetDirectionId());
    }

    @PostMapping("/{id}/transfer/accept")
    public ApplicationResponse acceptTransfer(@PathVariable Long id,
                                              Principal principal) {
        return service.acceptTransfer(id, AuthUser.id(principal));
    }

    @PostMapping("/{id}/transfer/reject")
    public ApplicationResponse rejectTransfer(@PathVariable Long id,
                                              Principal principal) {
        return service.rejectTransfer(id, AuthUser.id(principal));
    }
}
