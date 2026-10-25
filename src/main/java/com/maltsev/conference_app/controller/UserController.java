package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> list(@RequestParam(required = false) Role role) {
        return service.list(role);
    }
}
