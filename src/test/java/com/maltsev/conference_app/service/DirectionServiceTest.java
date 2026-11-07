package com.maltsev.conference_app.service;

import com.maltsev.conference_app.TestData;
import com.maltsev.conference_app.dto.DirectionResponse;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.repository.DirectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectionServiceTest {
    private static final OffsetDateTime DEADLINE = OffsetDateTime.parse("2026-12-01T12:00:00+03:00");

    @Mock
    private DirectionRepository directionRepository;
    @InjectMocks
    private DirectionService service;

    @Test
    void list_returnsDirectionsSortedByIdAsResponses() {
        Direction first = TestData.direction(1L, DEADLINE);
        Direction second = TestData.direction(2L, DEADLINE);
        when(directionRepository.findAll(Sort.by("id"))).thenReturn(List.of(first, second));

        List<DirectionResponse> result = service.list();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        assertEquals(100L, result.get(0).organizerId());
        assertNull(result.get(0).expertId());
    }

    @Test
    void list_withoutDirections_returnsEmptyList() {
        when(directionRepository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertTrue(service.list().isEmpty());
    }
}
