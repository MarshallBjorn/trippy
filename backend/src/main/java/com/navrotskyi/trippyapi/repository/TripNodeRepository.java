package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripNodeRepository extends JpaRepository<TripNode, UUID> {
    List<TripNode> findAllByEventIdOrderByStartTimeAsc(UUID eventId);
}