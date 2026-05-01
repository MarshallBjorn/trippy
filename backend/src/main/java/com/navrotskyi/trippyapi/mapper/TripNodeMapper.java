package com.navrotskyi.trippyapi.mapper;

import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.dto.TripNodeDto;

/**
 * Utility class do konwersji encji {@link TripNode} na {@link TripNodeDto}.
 * <p>
 * Wszystkie metody są statyczne. Konstruktor jest prywatny, aby uniemożliwić
 * instancjację (typowy wzorzec dla utility class).
 */
public final class TripNodeMapper {

    private TripNodeMapper() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
    }

    /**
     * Mapuje encję {@link TripNode} na {@link TripNodeDto}.
     * <p>
     * <b>Wymaganie:</b> encja musi mieć zainicjalizowane lazy-loaded relacje
     * {@code event} i {@code reporter} — w przeciwnym razie wystąpi
     * {@code LazyInitializationException}. Należy używać metod repozytorium
     * z {@code @EntityGraph} (np. {@code findWithDetailsById}).
     *
     * @param node encja TripNode (nie może być null)
     * @return zmapowany DTO
     * @throws NullPointerException jeśli {@code node} jest null
     */
    public static TripNodeDto toDto(TripNode node) {
        if (node == null) {
            throw new NullPointerException("Cannot map null TripNode to DTO.");
        }
        TripNodeDto dto = new TripNodeDto();
        dto.setId(node.getId());
        dto.setEventId(node.getEvent().getId());
        dto.setReporterId(node.getReporter().getId());
        dto.setReporterName(node.getReporter().getName());
        dto.setStartTime(node.getStartTime());
        dto.setEndTime(node.getEndTime());
        dto.setName(node.getName());
        dto.setNote(node.getNote());
        dto.setPrice(node.getPrice());
        dto.setSeparate(node.isSeparate());
        return dto;
    }
}