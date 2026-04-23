package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.dto.trip.ExpenseRequest;
import com.navrotskyi.trippyapi.dto.trip.ExpenseResponse;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Dodaje nowy wydatek (jako nowy węzeł - transakcję)
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            Principal principal) {
        TripNode createdNode = expenseService.createExpenseNode(request, principal.getName());
        
        ExpenseResponse response = new ExpenseResponse(
                createdNode.getId(),
                request.tripId(),
                request.title(),
                request.price(),
                request.isSeparate()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Aktualizuje istniejący węzeł przypisując do niego wartości wydatku
    @PatchMapping("/node/{nodeId}")
    public ResponseEntity<Void> assignExpenseToExistingNode(
            @PathVariable UUID nodeId,
            @Valid @RequestBody ExpenseRequest request,
            Principal principal) {
        
        expenseService.assignExpenseToNode(nodeId, request, principal.getName());
        return ResponseEntity.ok().build();
    }

    // Pobiera wszystkie wydatki (węzły) dla danej wycieczki
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<ExpenseResponse>> getTripExpenses(
            @PathVariable UUID tripId,
            Principal principal) {
        
        List<TripNode> nodes = expenseService.getExpensesByTrip(tripId, principal.getName());
        
        List<ExpenseResponse> response = nodes.stream()
                .map(node -> new ExpenseResponse(
                        node.getId(),
                        tripId,
                        node.getName(),
                        node.getPrice(),
                        node.isSeparate() // Jeśli używasz obiektu Boolean zamiast primitiva boolean, zmień na getSeparate() w przypadku błędu
                ))
                .toList();
                
        return ResponseEntity.ok(response);
    }
}
