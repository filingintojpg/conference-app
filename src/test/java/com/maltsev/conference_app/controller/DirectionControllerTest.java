package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.DirectionResponse;
import com.maltsev.conference_app.service.DirectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DirectionControllerTest {
    @Mock
    private DirectionService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DirectionController(service)).build();
    }

    @Test
    void list_returnsDirections() throws Exception {
        when(service.list()).thenReturn(List.of(
                new DirectionResponse(10L, "Programming", OffsetDateTime.parse("2027-01-01T12:00:00+03:00"), 100L, "Sidorova Maria")));

        mockMvc.perform(get("/directions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Programming"))
                .andExpect(jsonPath("$[0].organizerId").value(100))
                .andExpect(jsonPath("$[0].organizerFullName").value("Sidorova Maria"))
                .andExpect(jsonPath("$[0].deadline").exists());
    }
}
