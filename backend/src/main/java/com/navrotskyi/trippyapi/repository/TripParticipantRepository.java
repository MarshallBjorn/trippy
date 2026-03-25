package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripParticipantRepository extends JpaRepository<TripParticipant, UUID> {
    List<TripParticipant> findAllByEventId(UUID eventId);
    
    Optional<TripParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);
}