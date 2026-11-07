package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.AuthResponse;
import com.maltsev.conference_app.dto.LoginRequest;
import com.maltsev.conference_app.dto.RegisterRequest;
import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.security.AuthUser;
import com.maltsev.conference_app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return service.me(AuthUser.id(principal));
    }
}
