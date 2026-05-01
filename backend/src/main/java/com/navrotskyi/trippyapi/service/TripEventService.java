package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.CreateTripEventRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class TripEventService {

    private final TripEventRepository tripEventRepository;
    private final CurrencyRepository currencyRepository;
    private final TripRoleRepository tripRoleRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public TripEventService(TripEventRepository tripEventRepository, UserRepository userRepository, CurrencyRepository currencyRepository, TripRoleRepository tripRoleRepository, TripParticipantRepository tripParticipantRepository) {
        this.tripEventRepository = tripEventRepository;
        this.currencyRepository = currencyRepository;
        this.tripRoleRepository = tripRoleRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    @Transactional
    public TripEvent createTripEvent(CreateTripEventRequest request, User owner) {
        Currency currency = currencyRepository.findById(request.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + request.getCurrencyCode()));

        TripEvent tripEvent = new TripEvent();
        tripEvent.setName(request.getName());
        tripEvent.setOwner(owner);
        tripEvent.setCurrency(currency);
        tripEvent.setStartDate(request.getStartDate());
        tripEvent.setEndDate(request.getEndDate());
        tripEvent.setBudget(request.getBudget() != null ? request.getBudget() : BigDecimal.ZERO);
        tripEvent.setParticipants(new ArrayList<>());

        TripEvent savedTripEvent = tripEventRepository.save(tripEvent);

        TripRole organizerRole = tripRoleRepository.findByName("Organizer")
                .orElseThrow(() -> new IllegalStateException("'Organizer' role not found. Please seed the database."));

        TripParticipant ownerParticipant = new TripParticipant(
                savedTripEvent,
                owner,
                organizerRole,
                BigDecimal.ZERO,
                true
        );
        tripParticipantRepository.save(ownerParticipant);
        savedTripEvent.getParticipants().add(ownerParticipant);

        return savedTripEvent;
    }

    public TripEvent getTripEventById(UUID eventId) {
        return tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + eventId));
    }

    public List<TripEvent> getTripsByOwnerId(UUID ownerId) {
        return tripEventRepository.findAllByOwnerId(ownerId);
    }
    public List<TripEvent> getUserTrips(User user) {
    return tripEventRepository.findAllUserTrips(user);
}
}