package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.dto.CreateApplicationRequest;
import com.maltsev.conference_app.dto.UpdateApplicationRequest;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.Participant;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import com.maltsev.conference_app.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестируем логику сервиса без базы данных: репозитории подменены заглушками (@Mock).
 * when(...).thenReturn(...) говорит заглушке, что отвечать, а verify(...) проверяет,
 * вызывал ли сервис заглушку.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    private static final Long OWNER_ID = 1L;
    private static final Long STRANGER_ID = 2L;
    private static final Long OPEN_DIRECTION_ID = 10L;
    private static final Long CLOSED_DIRECTION_ID = 11L;
    private static final Long APPLICATION_ID = 5L;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private DirectionRepository directionRepository;

    private ApplicationService service;

    private Participant owner;
    private Direction openDirection;
    private Direction closedDirection;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(applicationRepository, participantRepository, directionRepository);
        owner = TestData.participant(OWNER_ID);
        openDirection = TestData.openDirection(OPEN_DIRECTION_ID);
        closedDirection = TestData.closedDirection(CLOSED_DIRECTION_ID);
    }

    private CreateApplicationRequest createRequest(Long directionId) {
        return new CreateApplicationRequest(directionId, "Spring Boot", "Abstract", "Content");
    }

    private UpdateApplicationRequest updateRequest() {
        return new UpdateApplicationRequest("New title", "New abstract", "New content");
    }

    /** Заглушка save(): как настоящая база, присваивает заявке id = 100. */
    private void stubSaveAssigningId() {
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L);
            return saved;
        });
    }

    private Application existingApplication(Direction direction) {
        Application application = TestData.application(APPLICATION_ID, owner, direction);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        return application;
    }

    private void assertApplicationUntouched(Application application) {
        assertEquals(TestData.TITLE, application.getTitle());
        assertEquals(TestData.ABSTRACT_TEXT, application.getAbstractText());
        assertEquals(TestData.CONTENT, application.getContent());
    }

    // =====================================================================
    //  create
    // =====================================================================

    @Test
    void create_success_savesApplicationWithDataFromRequest() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(openDirection));
        stubSaveAssigningId();

        service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository, times(1)).save(captor.capture());
        Application saved = captor.getValue();
        assertSame(owner, saved.getParticipant());
        assertSame(openDirection, saved.getDirection());
        assertEquals("Spring Boot", saved.getTitle());
        assertEquals("Abstract", saved.getAbstractText());
        assertEquals("Content", saved.getContent());
    }

    @Test
    void create_success_savedApplicationHasStatusSubmitted() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(openDirection));
        stubSaveAssigningId();

        service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertEquals(ApplicationStatus.SUBMITTED, captor.getValue().getStatus());
    }

    @Test
    void create_success_returnsResponseWithAllFields() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(openDirection));
        stubSaveAssigningId();

        ApplicationResponse response = service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));

        assertEquals(100L, response.id());
        assertEquals(OWNER_ID, response.participantId());
        assertEquals(OPEN_DIRECTION_ID, response.directionId());
        assertEquals("Spring Boot", response.title());
        assertEquals("Abstract", response.abstractText());
        assertEquals("Content", response.content());
        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }

    @Test
    void create_success_lookedUpParticipantAndDirectionByIds() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(openDirection));
        stubSaveAssigningId();

        service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));

        verify(participantRepository).findById(OWNER_ID);
        verify(directionRepository).findById(OPEN_DIRECTION_ID);
    }

    @Test
    void create_deadlineIsStillAMinuteAway_succeeds() {
        Direction almostClosed = TestData.direction(OPEN_DIRECTION_ID, OffsetDateTime.now().plusMinutes(1));
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(almostClosed));
        stubSaveAssigningId();

        ApplicationResponse response = service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));

        assertEquals(ApplicationStatus.SUBMITTED, response.status());
    }

    @Test
    void create_participantNotFound_throwsNotFound() {
        when(participantRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.create(99L, createRequest(OPEN_DIRECTION_ID)));

        assertEquals("Participant not found: 99", exception.getMessage());
    }

    @Test
    void create_participantNotFound_doesNotLookForDirectionAndDoesNotSave() {
        when(participantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(99L, createRequest(OPEN_DIRECTION_ID)));

        verifyNoInteractions(directionRepository);
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void create_directionNotFound_throwsNotFound() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.create(OWNER_ID, createRequest(99L)));

        assertEquals("Direction not found: 99", exception.getMessage());
    }

    @Test
    void create_directionNotFound_doesNotSave() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(OWNER_ID, createRequest(99L)));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void create_deadlinePassed_throwsConflict() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(CLOSED_DIRECTION_ID)).thenReturn(Optional.of(closedDirection));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.create(OWNER_ID, createRequest(CLOSED_DIRECTION_ID)));

        assertEquals("T1 has passed", exception.getMessage());
    }

    @Test
    void create_deadlinePassed_doesNotSave() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(CLOSED_DIRECTION_ID)).thenReturn(Optional.of(closedDirection));

        assertThrows(ConflictException.class, () -> service.create(OWNER_ID, createRequest(CLOSED_DIRECTION_ID)));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void create_deadlinePassedOneSecondAgo_throwsConflict() {
        Direction justClosed = TestData.direction(CLOSED_DIRECTION_ID, OffsetDateTime.now().minusSeconds(1));
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(CLOSED_DIRECTION_ID)).thenReturn(Optional.of(justClosed));

        assertThrows(ConflictException.class, () -> service.create(OWNER_ID, createRequest(CLOSED_DIRECTION_ID)));
    }

    // =====================================================================
    //  update
    // =====================================================================

    @Test
    void update_success_changesTitleAbstractAndContent() {
        Application application = existingApplication(openDirection);

        service.update(APPLICATION_ID, OWNER_ID, updateRequest());

        assertEquals("New title", application.getTitle());
        assertEquals("New abstract", application.getAbstractText());
        assertEquals("New content", application.getContent());
    }

    @Test
    void update_success_keepsStatusAndCreationTime() {
        Application application = existingApplication(openDirection);
        OffsetDateTime createdAt = application.getCreatedAt();

        service.update(APPLICATION_ID, OWNER_ID, updateRequest());

        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
        assertEquals(createdAt, application.getCreatedAt());
    }

    @Test
    void update_success_returnsResponseWithNewValues() {
        Application application = existingApplication(openDirection);

        ApplicationResponse response = service.update(APPLICATION_ID, OWNER_ID, updateRequest());

        assertEquals(APPLICATION_ID, response.id());
        assertEquals(OWNER_ID, response.participantId());
        assertEquals(OPEN_DIRECTION_ID, response.directionId());
        assertEquals("New title", response.title());
        assertEquals("New abstract", response.abstractText());
        assertEquals("New content", response.content());
        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertEquals(application.getCreatedAt(), response.createdAt());
        assertFalse(response.updatedAt().isBefore(response.createdAt()));
    }

    @Test
    void update_success_onlyLooksUpTheApplication() {
        existingApplication(openDirection);

        service.update(APPLICATION_ID, OWNER_ID, updateRequest());

        verify(applicationRepository).findById(APPLICATION_ID);
        verifyNoInteractions(participantRepository, directionRepository);
    }

    @Test
    void update_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.update(99L, OWNER_ID, updateRequest()));

        assertEquals("Application not found: 99", exception.getMessage());
    }

    @Test
    void update_foreignParticipant_throwsForbidden() {
        existingApplication(openDirection);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.update(APPLICATION_ID, STRANGER_ID, updateRequest()));

        assertEquals("Application belongs to another participant", exception.getMessage());
    }

    @Test
    void update_foreignParticipant_doesNotChangeApplication() {
        Application application = existingApplication(openDirection);

        assertThrows(ForbiddenException.class,
                () -> service.update(APPLICATION_ID, STRANGER_ID, updateRequest()));

        assertApplicationUntouched(application);
    }

    @Test
    void update_alreadyWithdrawn_throwsConflict() {
        Application application = existingApplication(openDirection);
        application.withdraw();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.update(APPLICATION_ID, OWNER_ID, updateRequest()));

        assertEquals("Application is already withdrawn", exception.getMessage());
    }

    @Test
    void update_alreadyWithdrawn_doesNotChangeApplication() {
        Application application = existingApplication(openDirection);
        application.withdraw();

        assertThrows(ConflictException.class, () -> service.update(APPLICATION_ID, OWNER_ID, updateRequest()));

        assertApplicationUntouched(application);
        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    }

    @Test
    void update_deadlinePassed_throwsConflict() {
        existingApplication(closedDirection);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.update(APPLICATION_ID, OWNER_ID, updateRequest()));

        assertEquals("T1 has passed", exception.getMessage());
    }

    @Test
    void update_deadlinePassed_doesNotChangeApplication() {
        Application application = existingApplication(closedDirection);

        assertThrows(ConflictException.class, () -> service.update(APPLICATION_ID, OWNER_ID, updateRequest()));

        assertApplicationUntouched(application);
    }

    @Test
    void update_foreignParticipantAndWithdrawn_forbiddenIsCheckedBeforeConflict() {
        Application application = existingApplication(openDirection);
        application.withdraw();

        assertThrows(ForbiddenException.class,
                () -> service.update(APPLICATION_ID, STRANGER_ID, updateRequest()));
    }

    @Test
    void update_foreignParticipantAndDeadlinePassed_forbiddenIsCheckedBeforeConflict() {
        existingApplication(closedDirection);

        assertThrows(ForbiddenException.class,
                () -> service.update(APPLICATION_ID, STRANGER_ID, updateRequest()));
    }

    @Test
    void update_twice_secondUpdateWins() {
        Application application = existingApplication(openDirection);

        service.update(APPLICATION_ID, OWNER_ID, new UpdateApplicationRequest("First", "First", "First"));
        service.update(APPLICATION_ID, OWNER_ID, new UpdateApplicationRequest("Second", "Second", "Second"));

        assertEquals("Second", application.getTitle());
        assertEquals("Second", application.getAbstractText());
        assertEquals("Second", application.getContent());
    }

    // =====================================================================
    //  withdraw
    // =====================================================================

    @Test
    void withdraw_success_setsStatusWithdrawn() {
        Application application = existingApplication(openDirection);

        service.withdraw(APPLICATION_ID, OWNER_ID);

        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    }

    @Test
    void withdraw_success_returnsResponseWithStatusWithdrawn() {
        Application application = existingApplication(openDirection);

        ApplicationResponse response = service.withdraw(APPLICATION_ID, OWNER_ID);

        assertEquals(APPLICATION_ID, response.id());
        assertEquals(OWNER_ID, response.participantId());
        assertEquals(OPEN_DIRECTION_ID, response.directionId());
        assertEquals(ApplicationStatus.WITHDRAWN, response.status());
        assertEquals(application.getCreatedAt(), response.createdAt());
        assertFalse(response.updatedAt().isBefore(response.createdAt()));
    }

    @Test
    void withdraw_success_keepsTexts() {
        Application application = existingApplication(openDirection);

        ApplicationResponse response = service.withdraw(APPLICATION_ID, OWNER_ID);

        assertApplicationUntouched(application);
        assertEquals(TestData.TITLE, response.title());
        assertEquals(TestData.ABSTRACT_TEXT, response.abstractText());
        assertEquals(TestData.CONTENT, response.content());
    }

    @Test
    void withdraw_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.withdraw(99L, OWNER_ID));

        assertEquals("Application not found: 99", exception.getMessage());
    }

    @Test
    void withdraw_foreignParticipant_throwsForbidden() {
        existingApplication(openDirection);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.withdraw(APPLICATION_ID, STRANGER_ID));

        assertEquals("Application belongs to another participant", exception.getMessage());
    }

    @Test
    void withdraw_foreignParticipant_keepsStatusSubmitted() {
        Application application = existingApplication(openDirection);

        assertThrows(ForbiddenException.class, () -> service.withdraw(APPLICATION_ID, STRANGER_ID));

        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
    }

    @Test
    void withdraw_alreadyWithdrawn_throwsConflict() {
        Application application = existingApplication(openDirection);
        application.withdraw();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.withdraw(APPLICATION_ID, OWNER_ID));

        assertEquals("Application is already withdrawn", exception.getMessage());
    }

    @Test
    void withdraw_deadlinePassed_throwsConflict() {
        existingApplication(closedDirection);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.withdraw(APPLICATION_ID, OWNER_ID));

        assertEquals("T1 has passed", exception.getMessage());
    }

    @Test
    void withdraw_deadlinePassed_keepsStatusSubmitted() {
        Application application = existingApplication(closedDirection);

        assertThrows(ConflictException.class, () -> service.withdraw(APPLICATION_ID, OWNER_ID));

        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
    }

    @Test
    void withdraw_foreignParticipantAndWithdrawn_forbiddenIsCheckedBeforeConflict() {
        Application application = existingApplication(openDirection);
        application.withdraw();

        assertThrows(ForbiddenException.class, () -> service.withdraw(APPLICATION_ID, STRANGER_ID));
    }

    @Test
    void withdraw_twice_secondCallThrowsConflict() {
        Application application = existingApplication(openDirection);

        ApplicationResponse first = service.withdraw(APPLICATION_ID, OWNER_ID);

        assertEquals(ApplicationStatus.WITHDRAWN, first.status());
        assertThrows(ConflictException.class, () -> service.withdraw(APPLICATION_ID, OWNER_ID));
        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    }

    // =====================================================================
    //  сценарий целиком
    // =====================================================================

    @Test
    void fullLifecycle_createUpdateWithdraw_thenNothingCanBeChanged() {
        when(participantRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(directionRepository.findById(OPEN_DIRECTION_ID)).thenReturn(Optional.of(openDirection));
        stubSaveAssigningId();

        // 1. создаём заявку
        ApplicationResponse created = service.create(OWNER_ID, createRequest(OPEN_DIRECTION_ID));
        assertEquals(ApplicationStatus.SUBMITTED, created.status());

        // репозиторий "хранит" то, что было сохранено
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application stored = captor.getValue();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(stored));

        // 2. редактируем
        ApplicationResponse updated = service.update(100L, OWNER_ID, updateRequest());
        assertEquals("New title", updated.title());
        assertEquals(ApplicationStatus.SUBMITTED, updated.status());

        // 3. отзываем
        ApplicationResponse withdrawn = service.withdraw(100L, OWNER_ID);
        assertEquals(ApplicationStatus.WITHDRAWN, withdrawn.status());
        assertEquals("New title", withdrawn.title());

        // 4. после отзыва нельзя ни править, ни отзывать повторно
        assertThrows(ConflictException.class, () -> service.update(100L, OWNER_ID, updateRequest()));
        assertThrows(ConflictException.class, () -> service.withdraw(100L, OWNER_ID));
    }
}
