package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.AuthResult;
import com.maltsev.conference_app.dto.LoginRequest;
import com.maltsev.conference_app.dto.RegisterRequest;
import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.GlobalExceptionHandler;
import com.maltsev.conference_app.exception.UnauthorizedException;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.security.AuthCookies;
import com.maltsev.conference_app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(service, new AuthCookies(false, "Strict")))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static AuthResult authResult() {
        return new AuthResult("jwt-token", 3600L,
                new UserResponse(1L, "Ivanov Ivan", "ivan@example.com", Role.PARTICIPANT));
    }

    private static final String VALID_REGISTER = """
            {"lastName":"Ivanov","firstName":"Ivan","email":"ivan@example.com","password":"secret-pass"}
            """;

    @Test
    void register_success_returns201AndSetsHttpOnlyCookie() throws Exception {
        when(service.register(any(RegisterRequest.class))).thenReturn(authResult());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Set-Cookie", containsString("access_token=jwt-token")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=3600")))
                .andExpect(jsonPath("$.role").value("PARTICIPANT"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_shortPassword_returns400WithFieldError() throws Exception {
        String json = """
                {"lastName":"Ivanov","firstName":"Ivan","email":"ivan@example.com","password":"short"}
                """;

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").value("password must be 8 to 72 characters"));

        verifyNoInteractions(service);
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        String json = """
                {"lastName":"Ivanov","firstName":"Ivan","email":"nope","password":"secret-pass"}
                """;

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());

        verifyNoInteractions(service);
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(service.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("User with this email already exists"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with this email already exists"));
    }

    @Test
    void login_success_setsCookieAndKeepsTokenOutOfBody() throws Exception {
        when(service.login(any(LoginRequest.class))).thenReturn(authResult());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"secret-pass\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("access_token=jwt-token")))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void login_badCredentials_doesNotSetCookie() throws Exception {
        when(service.login(any(LoginRequest.class))).thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Set-Cookie", not(containsString("access_token"))));
    }

    @Test
    void logout_returns204AndExpiresCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("access_token=;")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));

        verifyNoInteractions(service);
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(service.login(any(LoginRequest.class))).thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void login_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void me_returnsCurrentUser() throws Exception {
        when(service.me(1L)).thenReturn(new UserResponse(1L, "Ivanov Ivan", "ivan@example.com", Role.PARTICIPANT));

        mockMvc.perform(get("/auth/me").principal(Auth.user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void me_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }
}
