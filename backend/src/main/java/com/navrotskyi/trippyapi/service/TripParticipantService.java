package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.InviteParticipantRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TripParticipantService {

    private final TripParticipantRepository tripParticipantRepository;
    private final TripEventRepository tripEventRepository;
    private final UserRepository userRepository;
    private final TripRoleRepository tripRoleRepository;

    public TripParticipantService(TripParticipantRepository tripParticipantRepository, TripEventRepository tripEventRepository, UserRepository userRepository, TripRoleRepository tripRoleRepository) {
        this.tripParticipantRepository = tripParticipantRepository;
        this.tripEventRepository = tripEventRepository;
        this.userRepository = userRepository;
        this.tripRoleRepository = tripRoleRepository;
    }

    @Transactional
    public TripParticipant inviteParticipant(UUID eventId, InviteParticipantRequest request, User inviter) {
        TripEvent event = tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + eventId));

        if (!event.getOwner().getId().equals(inviter.getId())) {
            throw new SecurityException("Only the trip owner can invite participants.");
        }

        User userToInvite = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getUserEmail()));

        TripRole role = tripRoleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + request.getRoleName()));

        tripParticipantRepository.findByEventIdAndUserId(eventId, userToInvite.getId()).ifPresent(p -> {
            throw new IllegalStateException("User is already a participant in this trip.");
        });

        TripParticipant newParticipant = new TripParticipant(
                event,
                userToInvite,
                role,
                BigDecimal.ZERO,
                false
        );

        return tripParticipantRepository.save(newParticipant);
    }

    public List<TripParticipant> getParticipantsForEvent(UUID eventId) {
        return tripParticipantRepository.findAllByEventId(eventId);
    }
}