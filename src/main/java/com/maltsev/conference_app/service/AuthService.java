package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.AuthResponse;
import com.maltsev.conference_app.dto.LoginRequest;
import com.maltsev.conference_app.dto.RegisterRequest;
import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.exception.BadRequestException;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.exception.UnauthorizedException;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.UserRepository;
import com.maltsev.conference_app.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {
    private static final String BAD_CREDENTIALS = "Invalid email or password";
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String[] email = splitEmail(request.email()).orElseThrow(() -> new BadRequestException("Invalid email address"));

        if (email[0].length() > 64 || email[1].length() > 255) {
            throw new BadRequestException("Email address is too long");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BadRequestException("Password must be at most " + MAX_PASSWORD_BYTES + " bytes long");
        }
        if (userRepository.findByEmailLocalAndEmailDomain(email[0], email[1]).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }

        String middleName = request.middleName() == null || request.middleName().isBlank()
                ? null
                : request.middleName().trim();

        User user = userRepository.save(new User(
                request.lastName().trim(),
                request.firstName().trim(),
                middleName,
                email[0],
                email[1],
                Role.PARTICIPANT,
                passwordEncoder.encode(request.password())
        ));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<User> found = splitEmail(request.email()).flatMap(email -> userRepository.findByEmailLocalAndEmailDomain(email[0], email[1]));

        if (found.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new UnauthorizedException(BAD_CREDENTIALS);
        }
        User user = found.get();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException(BAD_CREDENTIALS);
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return UserResponse.from(user);
    }

    private AuthResponse toResponse(User user) {
        return AuthResponse.bearer(tokenService.issue(user), tokenService.expiresInSeconds(), UserResponse.from(user));
    }

    private static Optional<String[]> splitEmail(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String email = raw.trim().toLowerCase(Locale.ROOT);
        int at = email.lastIndexOf('@');
        if (at <= 0 || at == email.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(new String[]{email.substring(0, at), email.substring(at + 1)});
    }
}
