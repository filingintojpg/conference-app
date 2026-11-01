package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertApplicationServiceTest {
    private static final Long DIRECTION_ID = 10L;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private DirectionRepository directionRepository;

    private ExpertApplicationService service;
    private Application application;

    @BeforeEach
    void setUp() {
        service = new ExpertApplicationService(applicationRepository, directionRepository);
        Direction direction = TestData.openDirection(DIRECTION_ID);
        application = TestData.application(5L, TestData.participant(1L), direction);
        application.approve();
    }

    @Test
    void list_withoutDirection_asksForApprovedNotWithdrawnOfAllDirections() {
        when(applicationRepository.findByReviewStatusAndStatusNotOrderById(
                ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN)).thenReturn(List.of(application));

        List<ExpertApplicationResponse> result = service.list(null);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).id());
        verifyNoInteractions(directionRepository);
    }

    @Test
    void list_withDirection_filtersByDirection() {
        when(directionRepository.existsById(DIRECTION_ID)).thenReturn(true);
        when(applicationRepository.findByDirectionIdAndReviewStatusAndStatusNotOrderById(
                DIRECTION_ID, ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN)).thenReturn(List.of(application));

        assertEquals(1, service.list(DIRECTION_ID).size());
    }

    @Test
    void list_unknownDirection_throwsNotFound() {
        when(directionRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.list(99L));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    void response_hasNoParticipantData() {
        ExpertApplicationResponse response = ExpertApplicationResponse.from(application);

        assertEquals(TestData.TITLE, response.title());
        assertEquals("Programming", response.directionName());
        for (var component : ExpertApplicationResponse.class.getRecordComponents()) {
            assertFalse(component.getName().toLowerCase().contains("participant"));
        }
    }
}
