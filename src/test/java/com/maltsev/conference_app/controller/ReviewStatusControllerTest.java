package com.maltsev.conference_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewStatusControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ReviewStatusController()).build();

    @Test
    void list_returnsAllReviewStatuses() throws Exception {
        mockMvc.perform(get("/review-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("PENDING"))
                .andExpect(jsonPath("$[1]").value("APPROVED"))
                .andExpect(jsonPath("$[2]").value("REJECTED"));
    }
}
