package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.trip.CreateTripEventRequest;
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
        Currency currency = currencyRepository.findById(request.currencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + request.currencyCode()));

        TripEvent tripEvent = new TripEvent();
        tripEvent.setName(request.name());
        tripEvent.setOwner(owner);
        tripEvent.setCurrency(currency);
        tripEvent.setStartDate(request.startDate());
        tripEvent.setEndDate(request.endDate());
        tripEvent.setBudget(request.budget() != null ? request.budget() : BigDecimal.ZERO);
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
}