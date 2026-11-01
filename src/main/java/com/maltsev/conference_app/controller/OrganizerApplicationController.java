package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.TransferApplicationRequest;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.security.RequiresRole;
import com.maltsev.conference_app.security.UserHeader;
import com.maltsev.conference_app.service.OrganizerApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizer/applications")
@RequiresRole(Role.ORGANIZER)
public class OrganizerApplicationController {
    private final OrganizerApplicationService service;

    public OrganizerApplicationController(OrganizerApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationResponse> list(@RequestHeader(UserHeader.NAME) Long organizerId,
                                                   @RequestParam Long directionId,
                                                   @RequestParam(required = false) ApplicationStatus status,
                                                   @RequestParam(required = false) ReviewStatus reviewStatus) {
        return service.list(organizerId, directionId, status, reviewStatus);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id,
                                            @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.get(id, organizerId);
    }

    @PostMapping("/{id}/approve")
    public ApplicationResponse approve(@PathVariable Long id,
                                       @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.approve(id, organizerId);
    }

    @PostMapping("/{id}/reject")
    public ApplicationResponse reject(@PathVariable Long id,
                                      @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.reject(id, organizerId);
    }

    @PostMapping("/{id}/transfer")
    public ApplicationResponse transfer(@PathVariable Long id,
                                        @RequestHeader(UserHeader.NAME) Long organizerId,
                                        @Valid @RequestBody TransferApplicationRequest request) {
        return service.transfer(id, organizerId, request.targetDirectionId());
    }

    @PostMapping("/{id}/transfer/accept")
    public ApplicationResponse acceptTransfer(@PathVariable Long id,
                                              @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.acceptTransfer(id, organizerId);
    }

    @PostMapping("/{id}/transfer/reject")
    public ApplicationResponse rejectTransfer(@PathVariable Long id,
                                              @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.rejectTransfer(id, organizerId);
    }
}
