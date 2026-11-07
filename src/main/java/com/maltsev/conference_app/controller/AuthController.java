package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.AuthResult;
import com.maltsev.conference_app.dto.LoginRequest;
import com.maltsev.conference_app.dto.RegisterRequest;
import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.security.AuthCookies;
import com.maltsev.conference_app.security.AuthUser;
import com.maltsev.conference_app.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
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
    private final AuthCookies cookies;

    public AuthController(AuthService service, AuthCookies cookies) {
        this.service = service;
        this.cookies = cookies;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return signIn(service.register(request), response);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return signIn(service.login(request), response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        cookies.clear(response);
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return service.me(AuthUser.id(principal));
    }

    private UserResponse signIn(AuthResult result, HttpServletResponse response) {
        cookies.write(response, result.accessToken(), result.expiresIn());
        return result.user();
    }
}
