package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.AuthResponse;
import com.maltsev.conference_app.dto.LoginRequest;
import com.maltsev.conference_app.dto.RegisterRequest;
import com.maltsev.conference_app.exception.BadRequestException;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.UnauthorizedException;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.UserRepository;
import com.maltsev.conference_app.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, tokenService);
    }

    private static RegisterRequest registerRequest(String email, String password) {
        return new RegisterRequest("Ivanov", "Ivan", "Ivanovich", email, password);
    }

    private User userWithPassword(String rawPassword) {
        User user = new User("Ivanov", "Ivan", null, "ivan", "example.com", Role.PARTICIPANT,
                passwordEncoder.encode(rawPassword));
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    // ---------- регистрация ----------

    @Test
    void register_createsParticipantWithHashedPassword() {
        when(userRepository.findByEmailLocalAndEmailDomain("ivan", "example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(tokenService.issue(any(User.class))).thenReturn("jwt-token");
        when(tokenService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse response = service.register(registerRequest("  Ivan@Example.COM ", "secret-pass"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User user = saved.getValue();
        assertEquals(Role.PARTICIPANT, user.getRole());
        assertEquals("ivan", user.getEmailLocal());
        assertEquals("example.com", user.getEmailDomain());
        assertNotEquals("secret-pass", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret-pass", user.getPasswordHash()));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals("ivan@example.com", response.user().email());
    }

    @Test
    void register_existingEmail_throwsConflict() {
        when(userRepository.findByEmailLocalAndEmailDomain("ivan", "example.com"))
                .thenReturn(Optional.of(TestData.participant(1L)));

        assertThrows(ConflictException.class,
                () -> service.register(registerRequest("ivan@example.com", "secret-pass")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_invalidEmail_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.register(registerRequest("not-an-email", "secret-pass")));
        verifyNoInteractions(userRepository);
    }

    @Test
    void register_passwordLongerThan72Bytes_throwsBadRequest() {
        String longPassword = "я".repeat(40); // 80 байт в UTF-8

        assertThrows(BadRequestException.class,
                () -> service.register(registerRequest("ivan@example.com", longPassword)));
        verifyNoInteractions(userRepository);
    }

    // ---------- вход ----------

    @Test
    void login_correctPassword_returnsToken() {
        User user = userWithPassword("secret-pass");
        when(userRepository.findByEmailLocalAndEmailDomain("ivan", "example.com")).thenReturn(Optional.of(user));
        when(tokenService.issue(user)).thenReturn("jwt-token");
        when(tokenService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse response = service.login(new LoginRequest("Ivan@example.com", "secret-pass"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals(1L, response.user().id());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = userWithPassword("secret-pass");
        when(userRepository.findByEmailLocalAndEmailDomain("ivan", "example.com")).thenReturn(Optional.of(user));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("ivan@example.com", "wrong-pass")));
        assertEquals("Invalid email or password", ex.getMessage());
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_unknownEmail_throwsSameErrorAsWrongPassword() {
        when(userRepository.findByEmailLocalAndEmailDomain("nobody", "example.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("nobody@example.com", "whatever1")));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_malformedEmail_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("no-at-sign", "whatever1")));
        verify(userRepository, never()).findByEmailLocalAndEmailDomain(any(), any());
    }
}
