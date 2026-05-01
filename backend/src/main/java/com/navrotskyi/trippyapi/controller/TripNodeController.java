package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.CreateTripNodeRequest;
import com.navrotskyi.trippyapi.dto.TripNodeDto;
import com.navrotskyi.trippyapi.mapper.TripNodeMapper;
import com.navrotskyi.trippyapi.service.TripNodeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

/**
 * REST Controller zarządzający węzłami wycieczki ({@link TripNode}) — poszczególnymi
 * elementami planu (lot, hotel, atrakcja, posiłek, itp.).
 * <p>
 * Wszystkie endpointy zwracają spójny typ odpowiedzi: {@link TripNodeDto}.
 * Wszystkie wymagają uwierzytelnienia tokenem JWT.
 */
@RestController
@RequestMapping("/api/trips/{eventId}/nodes")
@Tag(name = "Trip Nodes", description = "Zarządzanie węzłami (elementami planu) wewnątrz wycieczki")
public class TripNodeController {

    private final TripNodeService tripNodeService;

    public TripNodeController(TripNodeService tripNodeService) {
        this.tripNodeService = tripNodeService;
    }

    // ============================================================
    //  CREATE
    // ============================================================

    @Operation(
            summary = "Utwórz nowy węzeł wycieczki",
            description = "Dodaje nowy węzeł (np. lot, nocleg, atrakcja) do wskazanej wycieczki. " +
                          "Tylko zaakceptowani uczestnicy mogą tworzyć węzły."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Węzeł utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych (np. endTime <= startTime, ujemna cena)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Brak ważnego tokena JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Użytkownik nie jest zaakceptowanym uczestnikiem wycieczki", content = @Content),
            @ApiResponse(responseCode = "404", description = "Wycieczka o podanym id nie istnieje", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TripNodeDto> createTripNode(
            @Parameter(description = "ID wycieczki") @PathVariable UUID eventId,
            @Valid @RequestBody CreateTripNodeRequest request,
            @AuthenticationPrincipal User currentUser) {
        TripNode created = tripNodeService.createTripNode(eventId, request, currentUser);
        return new ResponseEntity<>(TripNodeMapper.toDto(created), HttpStatus.CREATED);
    }




    // ============================================================
    //  READ — single
    // ============================================================

    @Operation(
            summary = "Pobierz pojedynczy węzeł",
            description = "Zwraca szczegóły konkretnego węzła. Tylko zaakceptowani uczestnicy mają dostęp."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szczegóły węzła"),
            @ApiResponse(responseCode = "400", description = "Węzeł nie należy do podanej wycieczki", content = @Content),
            @ApiResponse(responseCode = "401", description = "Brak ważnego tokena JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Użytkownik nie jest zaakceptowanym uczestnikiem wycieczki", content = @Content),
            @ApiResponse(responseCode = "404", description = "Węzeł o podanym id nie istnieje", content = @Content)
    })
    @GetMapping("/{nodeId}")
    public ResponseEntity<TripNodeDto> getTripNode(
            @Parameter(description = "ID wycieczki") @PathVariable UUID eventId,
            @Parameter(description = "ID węzła") @PathVariable UUID nodeId,
            @AuthenticationPrincipal User currentUser) {
        TripNode node = tripNodeService.getTripNode(eventId, nodeId, currentUser);
        return ResponseEntity.ok(TripNodeMapper.toDto(node));
    }

    // ============================================================
    //  UPDATE
    // ============================================================

    @Operation(
            summary = "Zaktualizuj węzeł",
            description = "Pełna aktualizacja (PUT semantics) wszystkich pól węzła. " +
                          "Tylko autor węzła lub organizator wycieczki ma uprawnienia."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Węzeł zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub węzeł nie należy do wycieczki", content = @Content),
            @ApiResponse(responseCode = "401", description = "Brak ważnego tokena JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Użytkownik nie jest autorem ani organizatorem", content = @Content),
            @ApiResponse(responseCode = "404", description = "Węzeł o podanym id nie istnieje", content = @Content)
    })
    @PutMapping("/{nodeId}")
    public ResponseEntity<TripNodeDto> updateTripNode(
            @Parameter(description = "ID wycieczki") @PathVariable UUID eventId,
            @Parameter(description = "ID węzła") @PathVariable UUID nodeId,
            @Valid @RequestBody CreateTripNodeRequest request,
            @AuthenticationPrincipal User currentUser) {
        TripNode updated = tripNodeService.updateTripNode(eventId, nodeId, request, currentUser);
        return ResponseEntity.ok(TripNodeMapper.toDto(updated));
    }

    // ============================================================
    //  DELETE
    // ============================================================

    @Operation(
            summary = "Usuń węzeł",
            description = "Trwale usuwa węzeł wraz z powiązanymi postami i zdjęciami " +
                          "(kaskadowe usuwanie). Tylko autor węzła lub organizator wycieczki ma uprawnienia."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Węzeł usunięty pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Węzeł nie należy do podanej wycieczki", content = @Content),
            @ApiResponse(responseCode = "401", description = "Brak ważnego tokena JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Użytkownik nie jest autorem ani organizatorem", content = @Content),
            @ApiResponse(responseCode = "404", description = "Węzeł o podanym id nie istnieje", content = @Content)
    })
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Void> deleteTripNode(
            @Parameter(description = "ID wycieczki") @PathVariable UUID eventId,
            @Parameter(description = "ID węzła") @PathVariable UUID nodeId,
            @AuthenticationPrincipal User currentUser) {
        tripNodeService.deleteTripNode(eventId, nodeId, currentUser);
        return ResponseEntity.noContent().build();
    }
}