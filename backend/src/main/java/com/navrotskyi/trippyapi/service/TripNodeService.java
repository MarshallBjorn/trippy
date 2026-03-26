package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.dto.CreateTripNodeRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TripNodeService {

    private final TripNodeRepository tripNodeRepository;
    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public TripNodeService(TripNodeRepository tripNodeRepository, TripEventRepository tripEventRepository, TripParticipantRepository tripParticipantRepository) {
        this.tripNodeRepository = tripNodeRepository;
        this.tripEventRepository = tripEventRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    @Transactional
    public TripNode createTripNode(UUID eventId, CreateTripNodeRequest request, User reporter) {
        TripEvent event = tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + eventId));

        tripParticipantRepository.findByEventIdAndUserId(eventId, reporter.getId())
                .filter(TripParticipant::isAccepted)
                .orElseThrow(() -> new SecurityException("Only accepted trip participants can add nodes."));

        TripNode node = new TripNode();
        node.setEvent(event);
        node.setReporter(reporter);
        node.setName(request.getName());
        node.setStartTime(request.getStartTime());
        node.setEndTime(request.getEndTime());
        node.setNote(request.getNote());
        node.setPrice(request.getPrice());
        node.setSeparate(request.isSeparate());

        return tripNodeRepository.save(node);
    }

    public List<TripNode> getNodesForEvent(UUID eventId, User user) {
        tripParticipantRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new SecurityException("You are not a participant of this trip."));
        return tripNodeRepository.findAllByEventIdOrderByStartTimeAsc(eventId);
    }
}