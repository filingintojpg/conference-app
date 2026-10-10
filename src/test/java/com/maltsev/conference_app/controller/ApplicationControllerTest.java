package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.GlobalExceptionHandler;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Проверяем HTTP-слой: адреса, заголовки, JSON, статусы и тексты ошибок.
 * Сервис подменён заглушкой, база и весь Spring-контекст не поднимаются.
 * MockMvc "отправляет" запросы контроллеру прямо из теста, без настоящего сервера.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {
    private static final String HEADER = "X-Participant-Id";

    @Mock
    private ApplicationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ApplicationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- вспомогательное ----------

    private static ApplicationResponse response(ApplicationStatus status) {
        OffsetDateTime time = OffsetDateTime.parse("2026-10-10T12:00:00+03:00");
        return new ApplicationResponse(100L, 1L, 10L, "Spring Boot", "Abstract", "Content", status, time, time);
    }

    private static String createJson(String directionId, String title, String abstractText, String content) {
        return """
                {
                  "directionId": %s,
                  "title": "%s",
                  "abstractText": "%s",
                  "content": "%s"
                }
                """.formatted(directionId, title, abstractText, content);
    }

    private static String validCreateJson() {
        return createJson("10", "Spring Boot", "Abstract", "Content");
    }

    private static String updateJson(String title, String abstractText, String content) {
        return """
                {
                  "title": "%s",
                  "abstractText": "%s",
                  "content": "%s"
                }
                """.formatted(title, abstractText, content);
    }

    private static String validUpdateJson() {
        return updateJson("New title", "New abstract", "New content");
    }

    // =====================================================================
    //  POST /applications
    // =====================================================================

    @Test
    void create_success_returns201WithApplicationJson() throws Exception {
        when(service.create(eq(1L), any(CreateApplicationRequest.class))).thenReturn(response(ApplicationStatus.SUBMITTED));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.participantId").value(1))
                .andExpect(jsonPath("$.directionId").value(10))
                .andExpect(jsonPath("$.title").value("Spring Boot"))
                .andExpect(jsonPath("$.abstractText").value("Abstract"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void create_success_passesHeaderAndBodyToService() throws Exception {
        when(service.create(eq(1L), any(CreateApplicationRequest.class))).thenReturn(response(ApplicationStatus.SUBMITTED));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateApplicationRequest> captor = ArgumentCaptor.forClass(CreateApplicationRequest.class);
        verify(service).create(eq(1L), captor.capture());
        assertEquals(10L, captor.getValue().directionId());
        assertEquals("Spring Boot", captor.getValue().title());
        assertEquals("Abstract", captor.getValue().abstractText());
        assertEquals("Content", captor.getValue().content());
    }

    @Test
    void create_titleOfExactly300Characters_isAccepted() throws Exception {
        when(service.create(eq(1L), any(CreateApplicationRequest.class))).thenReturn(response(ApplicationStatus.SUBMITTED));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("10", "a".repeat(300), "Abstract", "Content")))
                .andExpect(status().isCreated());
    }

    @Test
    void create_emptyTitle_returns400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("10", "", "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").value("title must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void create_titleOf301Characters_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("10", "a".repeat(301), "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").value("title must be at most 300 characters"));

        verifyNoInteractions(service);
    }

    @Test
    void create_emptyAbstractText_returns400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("10", "Title", "", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.abstractText").value("abstractText must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void create_emptyContent_returns400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("10", "Title", "Abstract", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.content").value("content must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void create_directionIdNull_returns400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("null", "Title", "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.directionId").value("directionId is required"));

        verifyNoInteractions(service);
    }

    @Test
    void create_emptyJsonObject_returns400WithAllFourFields() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.directionId").value("directionId is required"))
                .andExpect(jsonPath("$.fields.title").value("title must not be blank"))
                .andExpect(jsonPath("$.fields.abstractText").value("abstractText must not be blank"))
                .andExpect(jsonPath("$.fields.content").value("content must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void create_missingHeader_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Header X-Participant-Id is required"));

        verifyNoInteractions(service);
    }

    @Test
    void create_headerIsNotANumber_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'X-Participant-Id'"));

        verifyNoInteractions(service);
    }

    @Test
    void create_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"directionId\": 10, \"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));

        verifyNoInteractions(service);
    }

    @Test
    void create_directionIdIsNotANumber_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("\"abc\"", "Title", "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));

        verifyNoInteractions(service);
    }

    @Test
    void create_noBody_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));

        verifyNoInteractions(service);
    }

    @Test
    void create_bodyIsNotJson_returns415() throws Exception {
        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("just text"))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(service);
    }

    @Test
    void create_participantNotFound_returns404() throws Exception {
        when(service.create(eq(999L), any(CreateApplicationRequest.class)))
                .thenThrow(new NotFoundException("Participant not found: 999"));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Participant not found: 999"));
    }

    @Test
    void create_directionNotFound_returns404() throws Exception {
        when(service.create(eq(1L), any(CreateApplicationRequest.class)))
                .thenThrow(new NotFoundException("Direction not found: 10"));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Direction not found: 10"));
    }

    @Test
    void create_deadlinePassed_returns409() throws Exception {
        when(service.create(eq(1L), any(CreateApplicationRequest.class)))
                .thenThrow(new ConflictException("T1 has passed"));

        mockMvc.perform(post("/applications")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("T1 has passed"));
    }

    @Test
    void create_getMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(get("/applications").header(HEADER, "1"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  PUT /applications/{id}
    // =====================================================================

    @Test
    void update_success_returns200WithApplicationJson() throws Exception {
        when(service.update(eq(100L), eq(1L), any(UpdateApplicationRequest.class)))
                .thenReturn(response(ApplicationStatus.SUBMITTED));

        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.participantId").value(1))
                .andExpect(jsonPath("$.directionId").value(10))
                .andExpect(jsonPath("$.title").value("Spring Boot"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void update_success_passesIdHeaderAndBodyToService() throws Exception {
        when(service.update(eq(100L), eq(1L), any(UpdateApplicationRequest.class)))
                .thenReturn(response(ApplicationStatus.SUBMITTED));

        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateApplicationRequest> captor = ArgumentCaptor.forClass(UpdateApplicationRequest.class);
        verify(service).update(eq(100L), eq(1L), captor.capture());
        assertEquals("New title", captor.getValue().title());
        assertEquals("New abstract", captor.getValue().abstractText());
        assertEquals("New content", captor.getValue().content());
    }

    @Test
    void update_emptyTitle_returns400WithFieldMessage() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("", "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").value("title must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void update_titleOf301Characters_returns400() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("a".repeat(301), "Abstract", "Content")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").value("title must be at most 300 characters"));

        verifyNoInteractions(service);
    }

    @Test
    void update_emptyJsonObject_returns400WithAllThreeFields() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").value("title must not be blank"))
                .andExpect(jsonPath("$.fields.abstractText").value("abstractText must not be blank"))
                .andExpect(jsonPath("$.fields.content").value("content must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void update_missingHeader_returns400() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Header X-Participant-Id is required"));

        verifyNoInteractions(service);
    }

    @Test
    void update_headerIsNotANumber_returns400() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'X-Participant-Id'"));

        verifyNoInteractions(service);
    }

    @Test
    void update_idInUrlIsNotANumber_returns400() throws Exception {
        mockMvc.perform(put("/applications/abc")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'id'"));

        verifyNoInteractions(service);
    }

    @Test
    void update_malformedJson_returns400() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));

        verifyNoInteractions(service);
    }

    @Test
    void update_noBody_returns400() throws Exception {
        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));

        verifyNoInteractions(service);
    }

    @Test
    void update_applicationNotFound_returns404() throws Exception {
        when(service.update(eq(999L), eq(1L), any(UpdateApplicationRequest.class)))
                .thenThrow(new NotFoundException("Application not found: 999"));

        mockMvc.perform(put("/applications/999")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Application not found: 999"));
    }

    @Test
    void update_foreignParticipant_returns403() throws Exception {
        when(service.update(eq(100L), eq(2L), any(UpdateApplicationRequest.class)))
                .thenThrow(new ForbiddenException("Application belongs to another participant"));

        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Application belongs to another participant"));
    }

    @Test
    void update_alreadyWithdrawn_returns409() throws Exception {
        when(service.update(eq(100L), eq(1L), any(UpdateApplicationRequest.class)))
                .thenThrow(new ConflictException("Application is already withdrawn"));

        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Application is already withdrawn"));
    }

    @Test
    void update_deadlinePassed_returns409() throws Exception {
        when(service.update(eq(100L), eq(1L), any(UpdateApplicationRequest.class)))
                .thenThrow(new ConflictException("T1 has passed"));

        mockMvc.perform(put("/applications/100")
                        .header(HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("T1 has passed"));
    }

    @Test
    void update_deleteMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(delete("/applications/100").header(HEADER, "1"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  POST /applications/{id}/withdraw
    // =====================================================================

    @Test
    void withdraw_success_returns200WithStatusWithdrawn() throws Exception {
        when(service.withdraw(100L, 1L)).thenReturn(response(ApplicationStatus.WITHDRAWN));

        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.participantId").value(1))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    void withdraw_success_passesIdAndHeaderToService() throws Exception {
        when(service.withdraw(100L, 1L)).thenReturn(response(ApplicationStatus.WITHDRAWN));

        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "1"))
                .andExpect(status().isOk());

        verify(service).withdraw(100L, 1L);
    }

    @Test
    void withdraw_missingHeader_returns400() throws Exception {
        mockMvc.perform(post("/applications/100/withdraw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Header X-Participant-Id is required"));

        verifyNoInteractions(service);
    }

    @Test
    void withdraw_headerIsNotANumber_returns400() throws Exception {
        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'X-Participant-Id'"));

        verifyNoInteractions(service);
    }

    @Test
    void withdraw_idInUrlIsNotANumber_returns400() throws Exception {
        mockMvc.perform(post("/applications/abc/withdraw").header(HEADER, "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'id'"));

        verifyNoInteractions(service);
    }

    @Test
    void withdraw_applicationNotFound_returns404() throws Exception {
        when(service.withdraw(999L, 1L)).thenThrow(new NotFoundException("Application not found: 999"));

        mockMvc.perform(post("/applications/999/withdraw").header(HEADER, "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Application not found: 999"));
    }

    @Test
    void withdraw_foreignParticipant_returns403() throws Exception {
        when(service.withdraw(100L, 2L)).thenThrow(new ForbiddenException("Application belongs to another participant"));

        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Application belongs to another participant"));
    }

    @Test
    void withdraw_alreadyWithdrawn_returns409() throws Exception {
        when(service.withdraw(100L, 1L)).thenThrow(new ConflictException("Application is already withdrawn"));

        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Application is already withdrawn"));
    }

    @Test
    void withdraw_deadlinePassed_returns409() throws Exception {
        when(service.withdraw(100L, 1L)).thenThrow(new ConflictException("T1 has passed"));

        mockMvc.perform(post("/applications/100/withdraw").header(HEADER, "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("T1 has passed"));
    }

    @Test
    void withdraw_getMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(get("/applications/100/withdraw").header(HEADER, "1"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  прочее
    // =====================================================================

    @Test
    void unknownUrl_returns404() throws Exception {
        mockMvc.perform(get("/unknown").header(HEADER, "1"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }
}
