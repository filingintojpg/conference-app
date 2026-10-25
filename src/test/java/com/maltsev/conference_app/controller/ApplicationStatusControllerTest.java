package com.maltsev.conference_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationStatusControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApplicationStatusController()).build();

    @Test
    void list_returnsAllStatuses() throws Exception {
        mockMvc.perform(get("/application-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("SUBMITTED"))
                .andExpect(jsonPath("$[1]").value("EDITED"))
                .andExpect(jsonPath("$[2]").value("WITHDRAWN"));
    }
}
