package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.dto.admin.AdminTripResponse;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTripService {

    private final TripEventRepository tripEventRepository;

    public AdminTripService(TripEventRepository tripEventRepository) {
        this.tripEventRepository = tripEventRepository;
    }

    public List<AdminTripResponse> getAllTrips() {
        return tripEventRepository.findAll().stream()
                .map(trip -> new AdminTripResponse(
                        trip.getId(),
                        trip.getName(),
                        trip.getOwner().getName(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getBudget(),
                        trip.getCurrency().getCode()
                ))
                .collect(Collectors.toList());
    }
}