package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/application")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@RequestBody CreateApplicationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id,
                                      @RequestBody UpdateApplicationRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@PathVariable Long id) {
        return service.withdraw(id);
    }
}
