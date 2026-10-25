package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.exception.GlobalExceptionHandler;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_withoutRole_returnsAllUsers() throws Exception {
        when(service.list(null)).thenReturn(List.of(
                new UserResponse(1L, "Ivanov Ivan", "ivan@example.com", Role.PARTICIPANT),
                new UserResponse(2L, "Sidorova Maria", "maria@example.com", Role.ORGANIZER)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Ivanov Ivan"))
                .andExpect(jsonPath("$[0].role").value("PARTICIPANT"))
                .andExpect(jsonPath("$[1].email").value("maria@example.com"));
    }

    @Test
    void list_withRole_passesRoleToService() throws Exception {
        when(service.list(Role.ORGANIZER)).thenReturn(List.of(
                new UserResponse(2L, "Sidorova Maria", "maria@example.com", Role.ORGANIZER)));

        mockMvc.perform(get("/users").param("role", "ORGANIZER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("ORGANIZER"));
    }

    @Test
    void list_unknownRole_returns400() throws Exception {
        mockMvc.perform(get("/users").param("role", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'role'"));

        verifyNoInteractions(service);
    }
}
