package com.navrotskyi.trippyapi.service.admin;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripPost;
import com.navrotskyi.trippyapi.dto.admin.NodeResponse;
import com.navrotskyi.trippyapi.dto.admin.PhotoResponse;
import com.navrotskyi.trippyapi.dto.admin.PostResponse;
import com.navrotskyi.trippyapi.dto.admin.TripDetailsResponse;
import com.navrotskyi.trippyapi.dto.admin.TripResponse;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminTripService {

    private final TripEventRepository tripEventRepository;
    private final TripNodeRepository tripNodeRepository;

    public AdminTripService(TripEventRepository tripEventRepository, TripNodeRepository tripNodeRepository) {
        this.tripEventRepository = tripEventRepository;
        this.tripNodeRepository = tripNodeRepository;
    }

    @Value("${app.file.server.base.url:http://localhost:8888/uploads/}")
    private String fileServerBaseUrl;

    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {
        return tripEventRepository.findAll()
                .stream()
                .map(trip -> new TripResponse(
                        trip.getId(),
                        trip.getName(),
                        trip.getOwner().getName(),
                        trip.getOwner().getEmail(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getBudget(),
                        trip.getCurrency().getCode()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripDetailsResponse getTripDetails(UUID tripId) {
        TripEvent event = tripEventRepository.findById(tripId)
            .orElseThrow(() -> new EntityNotFoundException("Wyjazd nie istnieje"));

        List<TripNode> nodes = tripNodeRepository.findByEventId(tripId);

        List<NodeResponse> nodeResponses = nodes.stream()
            .map(this::mapToNodeResponse)
            .toList();

        return new TripDetailsResponse(
            event.getId(),
            event.getName(),
            event.getOwner().getName(),
            event.getStartDate(),
            event.getEndDate(),
            event.getBudget(),
            nodeResponses
        );
    }

    private NodeResponse mapToNodeResponse(TripNode node) {
        List<PostResponse> posts = node.getPosts().stream()
            .map(this::mapToPostResponse)
            .toList();
            
        return new NodeResponse(
            node.getId(), 
            node.getName(), 
            node.getNote(), 
            node.getPrice(),
            node.isSeparate(),
            node.getReporter().getName(),
            posts
        );
    }

    private PostResponse mapToPostResponse(TripPost post) {
        List<PhotoResponse> photos = post.getPhotos().stream()
            .map(p -> new PhotoResponse(p.getId(), fileServerBaseUrl + p.getPhotoUrl()))
            .toList();

        return new PostResponse(
            post.getId(), 
            post.getNote(),
            post.getReporter().getName(),
            photos
        );
    }
}