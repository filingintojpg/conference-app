package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.GlobalExceptionHandler;
import com.maltsev.conference_app.service.ExpertApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExpertApplicationControllerTest {
    private static final String URL = "/expert/applications";

    @Mock
    private ExpertApplicationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExpertApplicationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static ExpertApplicationResponse response(Long id) {
        OffsetDateTime time = OffsetDateTime.parse("2026-10-10T12:00:00+03:00");
        return new ExpertApplicationResponse(id, 10L, "Programming", "Spring Boot", "Abstract", "Content", time, time);
    }

    @Test
    void list_returnsApplicationsAndHasNoAuthorFields() throws Exception {
        when(service.list(7L)).thenReturn(List.of(response(1L)));

        mockMvc.perform(get(URL).principal(Auth.user("7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot"))
                .andExpect(jsonPath("$[0].participantId").doesNotExist())
                .andExpect(jsonPath("$[0].participantFullName").doesNotExist())
                .andExpect(jsonPath("$[0].participantEmail").doesNotExist());
    }

    @Test
    void list_passesUserIdToService() throws Exception {
        when(service.list(7L)).thenReturn(List.of());

        mockMvc.perform(get(URL).principal(Auth.user("7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).list(7L);
    }

    @Test
    void list_directionIdParameterIsIgnored() throws Exception {
        when(service.list(7L)).thenReturn(List.of());

        mockMvc.perform(get(URL).principal(Auth.user("7")).param("directionId", "99"))
                .andExpect(status().isOk());

        verify(service).list(7L);
    }

    @Test
    void list_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_tokenSubjectIsNotANumber_returns401() throws Exception {
        mockMvc.perform(get(URL).principal(Auth.user("abc")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_expertWithoutDirection_returns403() throws Exception {
        when(service.list(7L)).thenThrow(new ForbiddenException("Expert is not assigned to a direction"));

        mockMvc.perform(get(URL).principal(Auth.user("7")))
                .andExpect(status().isForbidden());
    }
}
