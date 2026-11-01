package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.ApplicationResponse;
import com.maltsev.conference_app.exception.ConflictException;
import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.ReviewStatus;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.ApplicationRepository;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестируем логику сервиса организатора без базы данных: репозитории подменены заглушками (@Mock).
 * Организатор видит только заявки тех направлений, за которые отвечает он сам.
 */
@ExtendWith(MockitoExtension.class)
class OrganizerApplicationServiceTest {
    /** Организатор, который отвечает за направление DIRECTION_ID (так устроен TestData.direction). */
    private static final Long ORGANIZER_ID = 100L;
    private static final Long STRANGER_ID = 200L;
    private static final Long DIRECTION_ID = 10L;
    private static final Long APPLICATION_ID = 5L;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private DirectionRepository directionRepository;

    private OrganizerApplicationService service;

    private User participant;
    private Direction direction;

    @BeforeEach
    void setUp() {
        service = new OrganizerApplicationService(applicationRepository, directionRepository);
        participant = TestData.participant(1L);
        direction = TestData.openDirection(DIRECTION_ID);
    }

    /** Заглушка: направление DIRECTION_ID существует. */
    private void directionExists() {
        when(directionRepository.findById(DIRECTION_ID)).thenReturn(Optional.of(direction));
    }

    /** Заглушка: репозиторий заявок возвращает переданные заявки для направления и статуса. */
    private void repositoryReturns(ApplicationStatus status, Application... applications) {
        when(applicationRepository.findByDirectionIdAndStatusOrderById(DIRECTION_ID, status))
                .thenReturn(List.of(applications));
    }

    /** Заявка в статусе SUBMITTED. */
    private Application submitted(Long id) {
        return TestData.application(id, participant, direction);
    }

    /** Заявка в статусе EDITED: статус меняет настоящий метод edit(). */
    private Application edited(Long id) {
        Application application = submitted(id);
        application.edit("Edited title", "Edited abstract", "Edited content");
        return application;
    }

    /** Заявка в статусе WITHDRAWN. */
    private Application withdrawn(Long id) {
        Application application = submitted(id);
        application.withdraw();
        return application;
    }

    // =====================================================================
    //  list
    // =====================================================================

    @Test
    void list_success_returnsApplicationsOfDirection() {
        directionExists();
        repositoryReturns(ApplicationStatus.SUBMITTED, submitted(1L), submitted(2L));

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void list_success_keepsOrderFromRepository() {
        directionExists();
        repositoryReturns(ApplicationStatus.SUBMITTED, submitted(3L), submitted(1L), submitted(2L));

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null);

        assertEquals(List.of(3L, 1L, 2L), result.stream().map(ApplicationResponse::id).toList());
    }

    @Test
    void list_success_responseHasAllFields() {
        directionExists();
        Application application = submitted(1L);
        repositoryReturns(ApplicationStatus.SUBMITTED, application);

        ApplicationResponse response = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null).get(0);

        assertEquals(1L, response.id());
        assertEquals(DIRECTION_ID, response.directionId());
        assertEquals("Programming", response.directionName());
        assertEquals(1L, response.participantId());
        assertEquals("Ivanov Ivan Ivanovich", response.participantFullName());
        assertEquals("ivan@example.com", response.participantEmail());
        assertEquals(TestData.TITLE, response.title());
        assertEquals(TestData.ABSTRACT_TEXT, response.abstractText());
        assertEquals(TestData.CONTENT, response.content());
        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertEquals(application.getCreatedAt(), response.createdAt());
        assertEquals(application.getUpdatedAt(), response.updatedAt());
    }

    @Test
    void list_success_editedApplicationShowsNewTextsAndStatusEdited() {
        directionExists();
        repositoryReturns(ApplicationStatus.EDITED, edited(1L));

        ApplicationResponse response = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.EDITED, null).get(0);

        assertEquals("Edited title", response.title());
        assertEquals("Edited abstract", response.abstractText());
        assertEquals("Edited content", response.content());
        assertEquals(ApplicationStatus.EDITED, response.status());
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void list_success_asksRepositoryForRequestedDirectionAndStatus(ApplicationStatus status) {
        directionExists();
        repositoryReturns(status);

        service.list(ORGANIZER_ID, DIRECTION_ID, status, null);

        verify(directionRepository).findById(DIRECTION_ID);
        verify(applicationRepository).findByDirectionIdAndStatusOrderById(DIRECTION_ID, status);
        verify(applicationRepository).findByTransferToIdOrderById(DIRECTION_ID);
        verifyNoMoreInteractions(applicationRepository, directionRepository);
    }

    @Test
    void list_noApplicationsWithThisStatus_returnsEmptyList() {
        directionExists();
        repositoryReturns(ApplicationStatus.WITHDRAWN);

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.WITHDRAWN, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void list_deadlinePassed_organizerStillSeesApplications() {
        Direction closed = TestData.closedDirection(DIRECTION_ID);
        when(directionRepository.findById(DIRECTION_ID)).thenReturn(Optional.of(closed));
        Application application = TestData.application(1L, participant, closed);
        repositoryReturns(ApplicationStatus.SUBMITTED, application);

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null);

        assertEquals(1, result.size());
    }

    @Test
    void list_directionNotFound_throwsNotFound() {
        when(directionRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.list(ORGANIZER_ID, 99L, ApplicationStatus.SUBMITTED, null));

        assertEquals("Direction not found: 99", exception.getMessage());
    }

    @Test
    void list_directionNotFound_doesNotLookForApplications() {
        when(directionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.list(ORGANIZER_ID, 99L, ApplicationStatus.SUBMITTED, null));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    void list_directionOfAnotherOrganizer_throwsForbidden() {
        directionExists();

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.list(STRANGER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null));

        assertEquals("Direction belongs to another organizer", exception.getMessage());
    }

    @Test
    void list_directionOfAnotherOrganizer_doesNotLookForApplications() {
        directionExists();

        assertThrows(ForbiddenException.class,
                () -> service.list(STRANGER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    void list_directionOfAnotherOrganizer_isForbiddenForEveryStatus() {
        directionExists();

        for (ApplicationStatus status : ApplicationStatus.values()) {
            assertThrows(ForbiddenException.class, () -> service.list(STRANGER_ID, DIRECTION_ID, status, null));
        }
    }

    @Test
    void list_accessIsDecidedByOrganizerOfTheDirection() {
        User otherOrganizer = TestData.organizer(STRANGER_ID);
        Direction foreignDirection = TestData.direction(20L, OffsetDateTime.now().plusDays(30), otherOrganizer);
        when(directionRepository.findById(20L)).thenReturn(Optional.of(foreignDirection));
        when(applicationRepository.findByDirectionIdAndStatusOrderById(20L, ApplicationStatus.SUBMITTED))
                .thenReturn(List.of());

        assertTrue(service.list(STRANGER_ID, 20L, ApplicationStatus.SUBMITTED, null).isEmpty());
        assertThrows(ForbiddenException.class, () -> service.list(ORGANIZER_ID, 20L, ApplicationStatus.SUBMITTED, null));
    }

    // =====================================================================
    //  get
    // =====================================================================

    @Test
    void get_success_returnsResponseWithAllFields() {
        Application application = submitted(APPLICATION_ID);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        ApplicationResponse response = service.get(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(APPLICATION_ID, response.id());
        assertEquals(DIRECTION_ID, response.directionId());
        assertEquals("Programming", response.directionName());
        assertEquals(1L, response.participantId());
        assertEquals("Ivanov Ivan Ivanovich", response.participantFullName());
        assertEquals("ivan@example.com", response.participantEmail());
        assertEquals(TestData.TITLE, response.title());
        assertEquals(TestData.ABSTRACT_TEXT, response.abstractText());
        assertEquals(TestData.CONTENT, response.content());
        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertEquals(application.getCreatedAt(), response.createdAt());
        assertEquals(application.getUpdatedAt(), response.updatedAt());
    }

    @Test
    void get_editedApplication_returnsStatusEdited() {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(edited(APPLICATION_ID)));

        ApplicationResponse response = service.get(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(ApplicationStatus.EDITED, response.status());
        assertEquals("Edited title", response.title());
    }

    @Test
    void get_withdrawnApplication_isStillVisibleToOrganizer() {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(withdrawn(APPLICATION_ID)));

        ApplicationResponse response = service.get(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(ApplicationStatus.WITHDRAWN, response.status());
    }

    @Test
    void get_deadlinePassed_organizerStillSeesApplication() {
        Direction closed = TestData.closedDirection(DIRECTION_ID);
        Application application = TestData.application(APPLICATION_ID, participant, closed);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        ApplicationResponse response = service.get(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(APPLICATION_ID, response.id());
    }

    @Test
    void get_success_onlyLooksUpTheApplication() {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(submitted(APPLICATION_ID)));

        service.get(APPLICATION_ID, ORGANIZER_ID);

        verify(applicationRepository).findById(APPLICATION_ID);
        verifyNoMoreInteractions(applicationRepository);
        verifyNoInteractions(directionRepository);
    }

    @Test
    void get_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.get(99L, ORGANIZER_ID));

        assertEquals("Application not found: 99", exception.getMessage());
    }

    @Test
    void get_directionOfAnotherOrganizer_throwsForbidden() {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(submitted(APPLICATION_ID)));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.get(APPLICATION_ID, STRANGER_ID));

        assertEquals("Direction belongs to another organizer", exception.getMessage());
    }

    @Test
    void get_directionOfAnotherOrganizer_isForbiddenForWithdrawnAndEditedToo() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(withdrawn(1L)));
        when(applicationRepository.findById(2L)).thenReturn(Optional.of(edited(2L)));

        assertThrows(ForbiddenException.class, () -> service.get(1L, STRANGER_ID));
        assertThrows(ForbiddenException.class, () -> service.get(2L, STRANGER_ID));
    }

    @Test
    void get_accessDependsOnDirectionOrganizerNotOnParticipant() {
        // Участник с тем же id, что у чужого организатора, доступа не даёт:
        // проверяется только организатор направления.
        User participantWithSameIdAsStranger = TestData.participant(STRANGER_ID);
        Application application = TestData.application(APPLICATION_ID, participantWithSameIdAsStranger, direction);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(ForbiddenException.class, () -> service.get(APPLICATION_ID, STRANGER_ID));
        assertEquals(APPLICATION_ID, service.get(APPLICATION_ID, ORGANIZER_ID).id());
    }

    // =====================================================================
    //  сценарий целиком
    // =====================================================================

    @Test
    void organizerSeesApplicationUnderItsCurrentStatusOnly() {
        // Одна и та же заявка последовательно проходит SUBMITTED -> EDITED -> WITHDRAWN.
        // Репозиторий подменён, поэтому "фильтр по статусу" имитируем вручную.
        Application application = submitted(APPLICATION_ID);
        directionExists();
        when(applicationRepository.findByDirectionIdAndStatusOrderById(eq(DIRECTION_ID), any(ApplicationStatus.class)))
                .thenAnswer(invocation -> {
                    ApplicationStatus requested = invocation.getArgument(1);
                    return application.getStatus() == requested ? List.of(application) : List.of();
                });

        assertEquals(1, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null).size());
        assertEquals(0, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.EDITED, null).size());

        application.edit("New title", "New abstract", "New content");

        assertEquals(0, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null).size());
        assertEquals(1, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.EDITED, null).size());

        application.withdraw();

        assertEquals(0, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.EDITED, null).size());
        List<ApplicationResponse> withdrawnList = service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.WITHDRAWN, null);
        assertEquals(1, withdrawnList.size());
        assertEquals("New title", withdrawnList.get(0).title());
    }

    // =====================================================================
    //  list: фильтр по результату проверки
    // =====================================================================

    @Test
    void list_noFilters_returnsAllApplicationsOfDirection() {
        directionExists();
        when(applicationRepository.findByDirectionIdOrderById(DIRECTION_ID))
                .thenReturn(List.of(submitted(1L), withdrawn(2L)));

        assertEquals(2, service.list(ORGANIZER_ID, DIRECTION_ID, null, null).size());
    }

    @Test
    void list_onlyReviewStatus_usesReviewStatusQuery() {
        directionExists();
        Application approved = submitted(1L);
        approved.approve();
        when(applicationRepository.findByDirectionIdAndReviewStatusOrderById(DIRECTION_ID, ReviewStatus.APPROVED))
                .thenReturn(List.of(approved));

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, null, ReviewStatus.APPROVED);

        assertEquals(1, result.size());
        assertEquals(ReviewStatus.APPROVED, result.get(0).reviewStatus());
    }

    @Test
    void list_bothFilters_usesCombinedQuery() {
        directionExists();
        when(applicationRepository.findByDirectionIdAndStatusAndReviewStatusOrderById(
                DIRECTION_ID, ApplicationStatus.EDITED, ReviewStatus.PENDING)).thenReturn(List.of(edited(1L)));

        List<ApplicationResponse> result =
                service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.EDITED, ReviewStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void list_responseContainsReviewStatus() {
        directionExists();
        repositoryReturns(ApplicationStatus.SUBMITTED, submitted(1L));

        assertEquals(ReviewStatus.PENDING, service.list(ORGANIZER_ID, DIRECTION_ID, ApplicationStatus.SUBMITTED, null).get(0).reviewStatus());
    }

    // =====================================================================
    //  approve / reject
    // =====================================================================

    @Test
    void approve_success_setsApprovedAndReturnsIt() {
        Application application = submitted(APPLICATION_ID);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        ApplicationResponse response = service.approve(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(ReviewStatus.APPROVED, response.reviewStatus());
        assertEquals(ReviewStatus.APPROVED, application.getReviewStatus());
        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
    }

    @Test
    void reject_success_setsRejectedAndReturnsIt() {
        Application application = edited(APPLICATION_ID);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        ApplicationResponse response = service.reject(APPLICATION_ID, ORGANIZER_ID);

        assertEquals(ReviewStatus.REJECTED, response.reviewStatus());
        assertEquals(ApplicationStatus.EDITED, application.getStatus());
    }

    @Test
    void approve_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.approve(99L, ORGANIZER_ID));
    }

    @Test
    void approve_directionOfAnotherOrganizer_throwsForbiddenAndKeepsPending() {
        Application application = submitted(APPLICATION_ID);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(ForbiddenException.class, () -> service.approve(APPLICATION_ID, STRANGER_ID));
        assertThrows(ForbiddenException.class, () -> service.reject(APPLICATION_ID, STRANGER_ID));

        assertEquals(ReviewStatus.PENDING, application.getReviewStatus());
    }

    @Test
    void approve_withdrawnApplication_throwsConflict() {
        Application application = withdrawn(APPLICATION_ID);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(ConflictException.class, () -> service.approve(APPLICATION_ID, ORGANIZER_ID));
        assertThrows(ConflictException.class, () -> service.reject(APPLICATION_ID, ORGANIZER_ID));

        assertEquals(ReviewStatus.PENDING, application.getReviewStatus());
    }

    @Test
    void review_alreadyApproved_cannotBeReviewedAgain() {
        Application application = submitted(APPLICATION_ID);
        application.approve();
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(ConflictException.class, () -> service.approve(APPLICATION_ID, ORGANIZER_ID));
        assertThrows(ConflictException.class, () -> service.reject(APPLICATION_ID, ORGANIZER_ID));

        assertEquals(ReviewStatus.APPROVED, application.getReviewStatus());
    }

    @Test
    void review_alreadyRejected_cannotBeReviewedAgain() {
        Application application = submitted(APPLICATION_ID);
        application.reject();
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(ConflictException.class, () -> service.approve(APPLICATION_ID, ORGANIZER_ID));

        assertEquals(ReviewStatus.REJECTED, application.getReviewStatus());
    }

    // =====================================================================
    //  передача заявок между направлениями
    // =====================================================================

    private static final Long TARGET_ID = 20L;
    private static final Long TARGET_ORGANIZER_ID = 300L;

    /** Направление другого организатора, на которое передают заявки. */
    private Direction target() {
        return TestData.direction(TARGET_ID, OffsetDateTime.now().plusDays(30), TestData.organizer(TARGET_ORGANIZER_ID));
    }

    private void targetExists(Direction target) {
        when(directionRepository.findById(TARGET_ID)).thenReturn(Optional.of(target));
    }

    private void applicationExists(Application application) {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
    }

    /** Заявка, переданная с направления DIRECTION_ID на направление target и ждущая ответа. */
    private Application transferredTo(Direction target) {
        Application application = submitted(APPLICATION_ID);
        application.requestTransfer(target);
        return application;
    }

    // ---------- transfer ----------

    @Test
    void transfer_success_marksApplicationAsWaitingForTarget() {
        Direction target = target();
        Application application = submitted(APPLICATION_ID);
        applicationExists(application);
        targetExists(target);

        ApplicationResponse response = service.transfer(APPLICATION_ID, ORGANIZER_ID, TARGET_ID);

        assertEquals(DIRECTION_ID, response.directionId());
        assertEquals(DIRECTION_ID, response.transferredFromDirectionId());
        assertEquals(TARGET_ID, response.transferToDirectionId());
        assertSame(target, application.getTransferTo());
        assertSame(direction, application.getDirection());
    }

    @Test
    void transfer_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.transfer(99L, ORGANIZER_ID, TARGET_ID));

        assertEquals("Application not found: 99", exception.getMessage());
    }

    @Test
    void transfer_directionOfAnotherOrganizer_throwsForbidden() {
        Application application = submitted(APPLICATION_ID);
        applicationExists(application);

        assertThrows(ForbiddenException.class, () -> service.transfer(APPLICATION_ID, STRANGER_ID, TARGET_ID));

        assertFalse(application.isTransferPending());
    }

    @Test
    void transfer_targetDirectionNotFound_throwsNotFound() {
        applicationExists(submitted(APPLICATION_ID));
        when(directionRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.transfer(APPLICATION_ID, ORGANIZER_ID, 99L));

        assertEquals("Direction not found: 99", exception.getMessage());
    }

    @Test
    void transfer_toTheSameDirection_throwsConflict() {
        Application application = submitted(APPLICATION_ID);
        applicationExists(application);
        when(directionRepository.findById(DIRECTION_ID)).thenReturn(Optional.of(direction));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.transfer(APPLICATION_ID, ORGANIZER_ID, DIRECTION_ID));

        assertEquals("Application already belongs to this direction", exception.getMessage());
        assertFalse(application.isTransferPending());
    }

    @Test
    void transfer_withdrawnApplication_throwsConflict() {
        Application application = withdrawn(APPLICATION_ID);
        applicationExists(application);
        targetExists(target());

        assertThrows(ConflictException.class, () -> service.transfer(APPLICATION_ID, ORGANIZER_ID, TARGET_ID));

        assertFalse(application.isTransferPending());
    }

    @Test
    void transfer_alreadyReviewedApplication_throwsConflict() {
        Application application = submitted(APPLICATION_ID);
        application.approve();
        applicationExists(application);
        targetExists(target());

        assertThrows(ConflictException.class, () -> service.transfer(APPLICATION_ID, ORGANIZER_ID, TARGET_ID));

        assertFalse(application.isTransferPending());
    }

    @Test
    void transfer_alreadyWaitingForDecision_throwsConflict() {
        Direction target = target();
        Application application = transferredTo(target);
        applicationExists(application);
        targetExists(target);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.transfer(APPLICATION_ID, ORGANIZER_ID, TARGET_ID));

        assertEquals("Application is already waiting for a transfer decision", exception.getMessage());
    }

    // ---------- acceptTransfer ----------

    @Test
    void acceptTransfer_success_movesApplicationToTargetDirection() {
        Direction target = target();
        Application application = transferredTo(target);
        applicationExists(application);

        ApplicationResponse response = service.acceptTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID);

        assertEquals(TARGET_ID, response.directionId());
        assertEquals(DIRECTION_ID, response.transferredFromDirectionId());
        assertNull(response.transferToDirectionId());
        assertSame(target, application.getDirection());
        assertEquals(ReviewStatus.PENDING, application.getReviewStatus());
    }

    @Test
    void acceptTransfer_sourceOrganizer_throwsForbidden() {
        Application application = transferredTo(target());
        applicationExists(application);

        assertThrows(ForbiddenException.class, () -> service.acceptTransfer(APPLICATION_ID, ORGANIZER_ID));

        assertTrue(application.isTransferPending());
        assertSame(direction, application.getDirection());
    }

    @Test
    void acceptTransfer_noPendingTransfer_throwsConflict() {
        applicationExists(submitted(APPLICATION_ID));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.acceptTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID));

        assertEquals("Application has no pending transfer", exception.getMessage());
    }

    @Test
    void acceptTransfer_withdrawnApplication_throwsConflict() {
        Application application = withdrawn(APPLICATION_ID);
        application.requestTransfer(target());
        applicationExists(application);

        assertThrows(ConflictException.class, () -> service.acceptTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID));

        assertSame(direction, application.getDirection());
    }

    @Test
    void acceptTransfer_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.acceptTransfer(99L, TARGET_ORGANIZER_ID));
    }

    // ---------- rejectTransfer ----------

    @Test
    void rejectTransfer_success_applicationStaysInSourceDirection() {
        Application application = transferredTo(target());
        applicationExists(application);

        ApplicationResponse response = service.rejectTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID);

        assertEquals(DIRECTION_ID, response.directionId());
        assertNull(response.transferToDirectionId());
        assertNull(response.transferredFromDirectionId());
        assertFalse(application.isTransferPending());
    }

    @Test
    void rejectTransfer_withdrawnApplication_canStillBeRejected() {
        Application application = withdrawn(APPLICATION_ID);
        application.requestTransfer(target());
        applicationExists(application);

        service.rejectTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID);

        assertFalse(application.isTransferPending());
    }

    @Test
    void rejectTransfer_sourceOrganizer_throwsForbidden() {
        Application application = transferredTo(target());
        applicationExists(application);

        assertThrows(ForbiddenException.class, () -> service.rejectTransfer(APPLICATION_ID, ORGANIZER_ID));

        assertTrue(application.isTransferPending());
    }

    @Test
    void rejectTransfer_noPendingTransfer_throwsConflict() {
        applicationExists(submitted(APPLICATION_ID));

        assertThrows(ConflictException.class, () -> service.rejectTransfer(APPLICATION_ID, TARGET_ORGANIZER_ID));
    }

    // ---------- влияние передачи на список, просмотр и проверку ----------

    @Test
    void list_targetDirection_showsIncomingTransferBeforeOwnApplications() {
        Direction target = target();
        when(directionRepository.findById(TARGET_ID)).thenReturn(Optional.of(target));
        Application incoming = transferredTo(target);
        Application own = TestData.application(7L, participant, target);
        when(applicationRepository.findByTransferToIdOrderById(TARGET_ID)).thenReturn(List.of(incoming));
        when(applicationRepository.findByDirectionIdOrderById(TARGET_ID)).thenReturn(List.of(own));

        List<ApplicationResponse> result = service.list(TARGET_ORGANIZER_ID, TARGET_ID, null, null);

        assertEquals(List.of(APPLICATION_ID, 7L), result.stream().map(ApplicationResponse::id).toList());
        assertEquals(DIRECTION_ID, result.get(0).transferredFromDirectionId());
        assertEquals(TARGET_ID, result.get(0).transferToDirectionId());
    }

    @Test
    void list_incomingTransfer_isFilteredByStatusAndReviewStatus() {
        Direction target = target();
        when(directionRepository.findById(TARGET_ID)).thenReturn(Optional.of(target));
        when(applicationRepository.findByTransferToIdOrderById(TARGET_ID))
                .thenReturn(List.of(transferredTo(target)));
        when(applicationRepository.findByDirectionIdAndStatusAndReviewStatusOrderById(
                TARGET_ID, ApplicationStatus.EDITED, ReviewStatus.PENDING)).thenReturn(List.of());

        List<ApplicationResponse> result = service.list(
                TARGET_ORGANIZER_ID, TARGET_ID, ApplicationStatus.EDITED, ReviewStatus.PENDING);

        assertTrue(result.isEmpty());
    }

    @Test
    void list_sourceDirection_stillShowsApplicationWaitingForDecision() {
        directionExists();
        Application application = transferredTo(target());
        when(applicationRepository.findByDirectionIdOrderById(DIRECTION_ID)).thenReturn(List.of(application));

        List<ApplicationResponse> result = service.list(ORGANIZER_ID, DIRECTION_ID, null, null);

        assertEquals(1, result.size());
        assertEquals(TARGET_ID, result.get(0).transferToDirectionId());
    }

    @Test
    void get_targetOrganizer_canViewApplicationWaitingForDecision() {
        applicationExists(transferredTo(target()));

        ApplicationResponse response = service.get(APPLICATION_ID, TARGET_ORGANIZER_ID);

        assertEquals(APPLICATION_ID, response.id());
        assertEquals(DIRECTION_ID, response.directionId());
    }

    @Test
    void get_targetOrganizer_cannotViewApplicationWithoutPendingTransfer() {
        applicationExists(submitted(APPLICATION_ID));

        assertThrows(ForbiddenException.class, () -> service.get(APPLICATION_ID, TARGET_ORGANIZER_ID));
    }

    @Test
    void review_applicationWaitingForTransferDecision_throwsConflict() {
        Application application = transferredTo(target());
        applicationExists(application);

        assertThrows(ConflictException.class, () -> service.approve(APPLICATION_ID, ORGANIZER_ID));
        assertThrows(ConflictException.class, () -> service.reject(APPLICATION_ID, ORGANIZER_ID));

        assertEquals(ReviewStatus.PENDING, application.getReviewStatus());
    }
}
