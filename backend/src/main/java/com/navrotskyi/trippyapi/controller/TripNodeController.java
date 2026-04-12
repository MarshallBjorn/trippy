package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.CreateTripNodeRequest;
import com.navrotskyi.trippyapi.dto.TripNodeDto;
import com.navrotskyi.trippyapi.dto.admin.NodeResponse;
import com.navrotskyi.trippyapi.mapper.TripNodeMapper;
import com.navrotskyi.trippyapi.service.TripNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.navrotskyi.trippyapi.dto.admin.NodeResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{eventId}/nodes")
@Tag(name = "Trip Nodes", description = "Endpoints for managing itinerary items (nodes) inside a trip")
public class TripNodeController {

    private final TripNodeService tripNodeService;

    public TripNodeController(TripNodeService tripNodeService) {
        this.tripNodeService = tripNodeService;
    }

    @PostMapping
    @Operation(summary = "Create a new trip node", description = "Adds a new node (e.g., flight, hotel, activity) to the specified trip event.")
    public ResponseEntity<TripNodeDto> createTripNode(@PathVariable UUID eventId, @RequestBody CreateTripNodeRequest request, @AuthenticationPrincipal User currentUser) {
        TripNode newNode = tripNodeService.createTripNode(eventId, request, currentUser);
        return new ResponseEntity<>(TripNodeMapper.toDto(newNode), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get trip nodes", description = "Retrieves all itinerary nodes for a specific trip event.")
    public ResponseEntity<List<NodeResponse>> getTripNodes(@PathVariable UUID eventId, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(tripNodeService.getNodesForEvent(eventId, currentUser));
    }

    @DeleteMapping("/{nodeId}")
    @Operation(summary = "Delete a trip node", description = "Deletes a specific node. Only the author or the ORGANIZER can do this.")
    public ResponseEntity<Void> deleteTripNode(
            @PathVariable UUID eventId, 
            @PathVariable UUID nodeId, 
            @AuthenticationPrincipal User currentUser) {
        tripNodeService.deleteTripNode(eventId, nodeId, currentUser);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{nodeId}")
    @Operation(summary = "Get single trip node details", description = "Retrieves details of a specific itinerary node.")
    public ResponseEntity<TripNodeDto> getTripNode(
            @PathVariable UUID eventId, 
            @PathVariable UUID nodeId, 
            @AuthenticationPrincipal User currentUser) {
        TripNode node = tripNodeService.getTripNode(nodeId, currentUser);
        return ResponseEntity.ok(TripNodeMapper.toDto(node));
    }


    @PutMapping("/{nodeId}")
    public ResponseEntity<TripNodeDto> updateNode(
        @PathVariable UUID eventId, 
        @PathVariable UUID nodeId, 
        @RequestBody CreateTripNodeRequest request, 
        @AuthenticationPrincipal User currentUser) {
    TripNode updated = tripNodeService.updateTripNode(eventId, nodeId, request, currentUser);
    return ResponseEntity.ok(TripNodeMapper.toDto(updated));
}

}