package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.security.RequiresRole;
import com.maltsev.conference_app.security.UserHeader;
import com.maltsev.conference_app.service.OrganizerApplicationService;
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
                                                   @RequestParam ApplicationStatus status) {
        return service.list(organizerId, directionId, status);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id,
                                            @RequestHeader(UserHeader.NAME) Long organizerId) {
        return service.get(id, organizerId);
    }
}
