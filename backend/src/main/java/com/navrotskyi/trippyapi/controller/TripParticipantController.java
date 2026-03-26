package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.InviteParticipantRequest;
import com.navrotskyi.trippyapi.dto.TripParticipantDto;
import com.navrotskyi.trippyapi.mapper.TripParticipantMapper;
import com.navrotskyi.trippyapi.service.TripParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips/{eventId}/participants")
@Tag(name = "Trip Participants", description = "Endpoints for managing trip participants")
public class TripParticipantController {

    private final TripParticipantService tripParticipantService;

    public TripParticipantController(TripParticipantService tripParticipantService) {
        this.tripParticipantService = tripParticipantService;
    }

    @PostMapping
    @Operation(summary = "Invite a participant", description = "Invites a user to a trip event using their email address.")
    public ResponseEntity<TripParticipantDto> inviteParticipant(@PathVariable UUID eventId, @RequestBody InviteParticipantRequest request, @AuthenticationPrincipal User currentUser) {
        TripParticipant newParticipant = tripParticipantService.inviteParticipant(eventId, request, currentUser);
        return new ResponseEntity<>(TripParticipantMapper.toDto(newParticipant), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get trip participants", description = "Retrieves a list of all participants for a specific trip event.")
    public ResponseEntity<List<TripParticipantDto>> getTripParticipants(@PathVariable UUID eventId) {
        List<TripParticipant> participants = tripParticipantService.getParticipantsForEvent(eventId);
        List<TripParticipantDto> participantDtos = participants.stream().map(TripParticipantMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(participantDtos);
    }
}