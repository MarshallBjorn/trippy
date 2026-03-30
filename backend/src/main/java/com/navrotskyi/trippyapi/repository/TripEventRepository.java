package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

@Repository
public interface TripEventRepository extends JpaRepository<TripEvent, UUID> {

    @EntityGraph(attributePaths = {
            "participants",
            "participants.user",
            "participants.tripRole",
            "currency"
    })
    List<TripEvent> findAllByOwnerId(UUID ownerId);

    @EntityGraph(attributePaths = {
            "participants",
            "participants.user",
            "participants.tripRole",
            "currency"
    })
    Optional<TripEvent> findById(UUID id);
}
