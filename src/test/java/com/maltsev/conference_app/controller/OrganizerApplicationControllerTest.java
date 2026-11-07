package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.GlobalExceptionHandler;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.service.OrganizerApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Проверяем HTTP-слой организатора: адреса, пользователь, параметры запроса, JSON, статусы и тексты ошибок.
 * Сервис подменён заглушкой, база и весь Spring-контекст не поднимаются.
 * <p>
 * Проверка роли (Spring Security, см. SecurityConfig) в MockMvc standalone-режиме не подключена,
 * вошедшего пользователя имитирует Principal. Здесь проверяется только сам контроллер.
 */
@ExtendWith(MockitoExtension.class)
class OrganizerApplicationControllerTest {
    private static final String LIST_URL = "/organizer/applications";

    @Mock
    private OrganizerApplicationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrganizerApplicationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- вспомогательное ----------

    private static ApplicationResponse response(Long id, ApplicationStatus status) {
        OffsetDateTime time = OffsetDateTime.parse("2026-10-10T12:00:00+03:00");
        return new ApplicationResponse(id, 10L, "Programming", 1L, "Ivanov Ivan Ivanovich", "ivan@example.com",
                "Spring Boot", "Abstract", "Content", status, ReviewStatus.PENDING, time, time);
    }

    // =====================================================================
    //  GET /organizer/applications?directionId=...&status=...
    // =====================================================================

    @Test
    void list_success_returns200WithJsonArray() throws Exception {
        when(service.list(100L, 10L, ApplicationStatus.SUBMITTED, null))
                .thenReturn(List.of(response(1L, ApplicationStatus.SUBMITTED), response(2L, ApplicationStatus.SUBMITTED)));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void list_success_eachItemHasAllFields() throws Exception {
        when(service.list(100L, 10L, ApplicationStatus.SUBMITTED, null))
                .thenReturn(List.of(response(1L, ApplicationStatus.SUBMITTED)));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].directionId").value(10))
                .andExpect(jsonPath("$[0].directionName").value("Programming"))
                .andExpect(jsonPath("$[0].participantId").value(1))
                .andExpect(jsonPath("$[0].participantFullName").value("Ivanov Ivan Ivanovich"))
                .andExpect(jsonPath("$[0].participantEmail").value("ivan@example.com"))
                .andExpect(jsonPath("$[0].title").value("Spring Boot"))
                .andExpect(jsonPath("$[0].abstractText").value("Abstract"))
                .andExpect(jsonPath("$[0].content").value("Content"))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void list_success_passesUserIdAndParametersToService() throws Exception {
        when(service.list(100L, 10L, ApplicationStatus.EDITED, null)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "EDITED"))
                .andExpect(status().isOk());

        verify(service).list(100L, 10L, ApplicationStatus.EDITED, null);
        verifyNoMoreInteractions(service);
    }

    @Test
    void list_noApplications_returns200WithEmptyArray() throws Exception {
        when(service.list(100L, 10L, ApplicationStatus.SUBMITTED, null)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void list_everyStatusIsAccepted_andReturnedInJson(ApplicationStatus applicationStatus) throws Exception {
        when(service.list(100L, 10L, applicationStatus, null)).thenReturn(List.of(response(1L, applicationStatus)));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", applicationStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(applicationStatus.name()));
    }

    @Test
    void list_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }

    @Test
    void list_tokenSubjectIsNotANumber_returns401() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("abc"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));

        verifyNoInteractions(service);
    }

    @Test
    void list_missingDirectionId_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("status", "SUBMITTED"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void list_missingStatus_isAllowedAndMeansAnyStatus() throws Exception {
        when(service.list(100L, 10L, null, null)).thenReturn(List.of(response(1L, ApplicationStatus.EDITED)));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void list_noParameters_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL).principal(Auth.user("100")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void list_directionIdIsNotANumber_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "abc")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'directionId'"));

        verifyNoInteractions(service);
    }

    @Test
    void list_unknownStatus_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'status'"));

        verifyNoInteractions(service);
    }

    @Test
    void list_statusInLowerCase_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "submitted"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'status'"));

        verifyNoInteractions(service);
    }

    @Test
    void list_directionNotFound_returns404() throws Exception {
        when(service.list(100L, 999L, ApplicationStatus.SUBMITTED, null))
                .thenThrow(new NotFoundException("Direction not found: 999"));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "999")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Direction not found: 999"));
    }

    @Test
    void list_directionOfAnotherOrganizer_returns403() throws Exception {
        when(service.list(200L, 10L, ApplicationStatus.SUBMITTED, null))
                .thenThrow(new ForbiddenException("Direction belongs to another organizer"));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("200"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Direction belongs to another organizer"));
    }

    @Test
    void list_postMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(post(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  GET /organizer/applications/{id}
    // =====================================================================

    @Test
    void get_success_returns200WithApplicationJson() throws Exception {
        when(service.get(5L, 100L)).thenReturn(response(5L, ApplicationStatus.SUBMITTED));

        mockMvc.perform(get(LIST_URL + "/5").principal(Auth.user("100")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.directionId").value(10))
                .andExpect(jsonPath("$.directionName").value("Programming"))
                .andExpect(jsonPath("$.participantId").value(1))
                .andExpect(jsonPath("$.participantFullName").value("Ivanov Ivan Ivanovich"))
                .andExpect(jsonPath("$.participantEmail").value("ivan@example.com"))
                .andExpect(jsonPath("$.title").value("Spring Boot"))
                .andExpect(jsonPath("$.abstractText").value("Abstract"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void get_success_passesIdAndHeaderToService() throws Exception {
        when(service.get(5L, 100L)).thenReturn(response(5L, ApplicationStatus.SUBMITTED));

        mockMvc.perform(get(LIST_URL + "/5").principal(Auth.user("100")))
                .andExpect(status().isOk());

        verify(service).get(5L, 100L);
        verifyNoMoreInteractions(service);
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void get_everyStatusIsReturnedInJson(ApplicationStatus applicationStatus) throws Exception {
        when(service.get(5L, 100L)).thenReturn(response(5L, applicationStatus));

        mockMvc.perform(get(LIST_URL + "/5").principal(Auth.user("100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(applicationStatus.name()));
    }

    @Test
    void get_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(get(LIST_URL + "/5"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }

    @Test
    void get_tokenSubjectIsNotANumber_returns401() throws Exception {
        mockMvc.perform(get(LIST_URL + "/5").principal(Auth.user("abc")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));

        verifyNoInteractions(service);
    }

    @Test
    void get_idInUrlIsNotANumber_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL + "/abc").principal(Auth.user("100")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'id'"));

        verifyNoInteractions(service);
    }

    @Test
    void get_applicationNotFound_returns404() throws Exception {
        when(service.get(999L, 100L)).thenThrow(new NotFoundException("Application not found: 999"));

        mockMvc.perform(get(LIST_URL + "/999").principal(Auth.user("100")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Application not found: 999"));
    }

    @Test
    void get_directionOfAnotherOrganizer_returns403() throws Exception {
        when(service.get(5L, 200L)).thenThrow(new ForbiddenException("Direction belongs to another organizer"));

        mockMvc.perform(get(LIST_URL + "/5").principal(Auth.user("200")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Direction belongs to another organizer"));
    }

    @Test
    void get_postMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5").principal(Auth.user("100")))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    @Test
    void get_deleteMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(delete(LIST_URL + "/5").principal(Auth.user("100")))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  прочее
    // =====================================================================

    @Test
    void unknownUrl_returns404() throws Exception {
        mockMvc.perform(get("/organizer/unknown").principal(Auth.user("100")))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  проверка заявок организатором
    // =====================================================================

    private static ApplicationResponse reviewed(Long id, ReviewStatus reviewStatus) {
        OffsetDateTime time = OffsetDateTime.parse("2026-10-10T12:00:00+03:00");
        return new ApplicationResponse(id, 10L, "Programming", 1L, "Ivanov Ivan Ivanovich", "ivan@example.com",
                "Spring Boot", "Abstract", "Content", ApplicationStatus.SUBMITTED, reviewStatus, time, time);
    }

    @Test
    void list_reviewStatusParameter_isPassedToService() throws Exception {
        when(service.list(100L, 10L, null, ReviewStatus.REJECTED))
                .thenReturn(List.of(reviewed(1L, ReviewStatus.REJECTED)));

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("reviewStatus", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewStatus").value("REJECTED"));
    }

    @Test
    void list_bothFilters_arePassedToService() throws Exception {
        when(service.list(100L, 10L, ApplicationStatus.EDITED, ReviewStatus.PENDING)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("status", "EDITED")
                        .param("reviewStatus", "PENDING"))
                .andExpect(status().isOk());

        verify(service).list(100L, 10L, ApplicationStatus.EDITED, ReviewStatus.PENDING);
    }

    @Test
    void list_unknownReviewStatus_returns400() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .principal(Auth.user("100"))
                        .param("directionId", "10")
                        .param("reviewStatus", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'reviewStatus'"));

        verifyNoInteractions(service);
    }

    @Test
    void approve_success_returnsApplicationWithApprovedStatus() throws Exception {
        when(service.approve(5L, 100L)).thenReturn(reviewed(5L, ReviewStatus.APPROVED));

        mockMvc.perform(post(LIST_URL + "/5/approve").principal(Auth.user("100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.reviewStatus").value("APPROVED"));

        verify(service).approve(5L, 100L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void reject_success_returnsApplicationWithRejectedStatus() throws Exception {
        when(service.reject(5L, 100L)).thenReturn(reviewed(5L, ReviewStatus.REJECTED));

        mockMvc.perform(post(LIST_URL + "/5/reject").principal(Auth.user("100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewStatus").value("REJECTED"));

        verify(service).reject(5L, 100L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void approve_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5/approve"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }

    @Test
    void approve_applicationNotFound_returns404() throws Exception {
        when(service.approve(999L, 100L)).thenThrow(new NotFoundException("Application not found: 999"));

        mockMvc.perform(post(LIST_URL + "/999/approve").principal(Auth.user("100")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Application not found: 999"));
    }

    @Test
    void reject_directionOfAnotherOrganizer_returns403() throws Exception {
        when(service.reject(5L, 200L)).thenThrow(new ForbiddenException("Direction belongs to another organizer"));

        mockMvc.perform(post(LIST_URL + "/5/reject").principal(Auth.user("200")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Direction belongs to another organizer"));
    }

    @Test
    void approve_alreadyReviewed_returns409() throws Exception {
        when(service.approve(5L, 100L)).thenThrow(new ConflictException("Application has already been reviewed"));

        mockMvc.perform(post(LIST_URL + "/5/approve").principal(Auth.user("100")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Application has already been reviewed"));
    }

    @Test
    void approve_getMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(get(LIST_URL + "/5/approve").principal(Auth.user("100")))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }

    // =====================================================================
    //  передача заявок между направлениями
    // =====================================================================

    private static ApplicationResponse transferred(Long id, Long fromId, Long toId) {
        OffsetDateTime time = OffsetDateTime.parse("2026-10-10T12:00:00+03:00");
        return new ApplicationResponse(id, fromId, "Programming", 1L, "Ivanov Ivan Ivanovich", "ivan@example.com",
                "Spring Boot", "Abstract", "Content", ApplicationStatus.SUBMITTED, ReviewStatus.PENDING, time, time,
                fromId, "Programming", toId, toId == null ? null : "Biology");
    }

    @Test
    void transfer_success_returnsApplicationWaitingForDecision() throws Exception {
        when(service.transfer(5L, 100L, 20L)).thenReturn(transferred(5L, 10L, 20L));

        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .principal(Auth.user("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDirectionId\": 20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.directionId").value(10))
                .andExpect(jsonPath("$.transferredFromDirectionId").value(10))
                .andExpect(jsonPath("$.transferredFromDirectionName").value("Programming"))
                .andExpect(jsonPath("$.transferToDirectionId").value(20))
                .andExpect(jsonPath("$.transferToDirectionName").value("Biology"));

        verify(service).transfer(5L, 100L, 20L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void transfer_missingTargetDirection_returns400() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .principal(Auth.user("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.targetDirectionId").value("targetDirectionId is required"));

        verifyNoInteractions(service);
    }

    @Test
    void transfer_missingBody_returns400() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5/transfer").principal(Auth.user("100")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void transfer_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDirectionId\": 20}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }

    @Test
    void transfer_targetDirectionNotFound_returns404() throws Exception {
        when(service.transfer(5L, 100L, 99L)).thenThrow(new NotFoundException("Direction not found: 99"));

        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .principal(Auth.user("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDirectionId\": 99}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Direction not found: 99"));
    }

    @Test
    void transfer_directionOfAnotherOrganizer_returns403() throws Exception {
        when(service.transfer(5L, 200L, 20L)).thenThrow(new ForbiddenException("Direction belongs to another organizer"));

        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .principal(Auth.user("200"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDirectionId\": 20}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Direction belongs to another organizer"));
    }

    @Test
    void transfer_alreadyWaitingForDecision_returns409() throws Exception {
        when(service.transfer(5L, 100L, 20L))
                .thenThrow(new ConflictException("Application is already waiting for a transfer decision"));

        mockMvc.perform(post(LIST_URL + "/5/transfer")
                        .principal(Auth.user("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDirectionId\": 20}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Application is already waiting for a transfer decision"));
    }

    @Test
    void acceptTransfer_success_returnsApplicationInNewDirection() throws Exception {
        when(service.acceptTransfer(5L, 300L)).thenReturn(transferred(5L, 20L, null));

        mockMvc.perform(post(LIST_URL + "/5/transfer/accept").principal(Auth.user("300")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.directionId").value(20))
                .andExpect(jsonPath("$.transferToDirectionId").doesNotExist());

        verify(service).acceptTransfer(5L, 300L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void acceptTransfer_noPendingTransfer_returns409() throws Exception {
        when(service.acceptTransfer(5L, 300L)).thenThrow(new ConflictException("Application has no pending transfer"));

        mockMvc.perform(post(LIST_URL + "/5/transfer/accept").principal(Auth.user("300")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Application has no pending transfer"));
    }

    @Test
    void acceptTransfer_notTheTargetOrganizer_returns403() throws Exception {
        when(service.acceptTransfer(5L, 100L)).thenThrow(new ForbiddenException("Direction belongs to another organizer"));

        mockMvc.perform(post(LIST_URL + "/5/transfer/accept").principal(Auth.user("100")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Direction belongs to another organizer"));
    }

    @Test
    void acceptTransfer_missingAuthentication_returns401() throws Exception {
        mockMvc.perform(post(LIST_URL + "/5/transfer/accept"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verifyNoInteractions(service);
    }

    @Test
    void rejectTransfer_success_returnsApplicationInSourceDirection() throws Exception {
        when(service.rejectTransfer(5L, 300L)).thenReturn(transferred(5L, 10L, null));

        mockMvc.perform(post(LIST_URL + "/5/transfer/reject").principal(Auth.user("300")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directionId").value(10))
                .andExpect(jsonPath("$.transferToDirectionId").doesNotExist());

        verify(service).rejectTransfer(5L, 300L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void rejectTransfer_applicationNotFound_returns404() throws Exception {
        when(service.rejectTransfer(999L, 300L)).thenThrow(new NotFoundException("Application not found: 999"));

        mockMvc.perform(post(LIST_URL + "/999/transfer/reject").principal(Auth.user("300")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Application not found: 999"));
    }

    @Test
    void transferEndpoints_getMethodIsNotAllowed_returns405() throws Exception {
        mockMvc.perform(get(LIST_URL + "/5/transfer").principal(Auth.user("100")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get(LIST_URL + "/5/transfer/accept").principal(Auth.user("300")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get(LIST_URL + "/5/transfer/reject").principal(Auth.user("300")))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }
}
