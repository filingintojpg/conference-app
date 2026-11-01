package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.ExpertApplicationResponse;
import com.maltsev.conference_app.exception.ForbiddenException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertApplicationServiceTest {
    private static final Long DIRECTION_ID = 10L;
    private static final Long EXPERT_ID = 7L;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private DirectionRepository directionRepository;

    private ExpertApplicationService service;
    private Direction direction;
    private Application application;

    @BeforeEach
    void setUp() {
        service = new ExpertApplicationService(applicationRepository, directionRepository);
        direction = TestData.openDirection(DIRECTION_ID);
        application = TestData.application(5L, TestData.participant(1L), direction);
        application.approve();
    }

    @Test
    void list_returnsApprovedNotWithdrawnApplicationsOfExpertsDirectionOnly() {
        when(directionRepository.findByExpertId(EXPERT_ID)).thenReturn(Optional.of(direction));
        when(applicationRepository.findByDirectionIdAndReviewStatusAndStatusNotOrderById(
                DIRECTION_ID, ReviewStatus.APPROVED, ApplicationStatus.WITHDRAWN)).thenReturn(List.of(application));

        List<ExpertApplicationResponse> result = service.list(EXPERT_ID);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).id());
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void list_expertWithoutDirection_throwsForbidden() {
        when(directionRepository.findByExpertId(EXPERT_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.list(EXPERT_ID));
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
