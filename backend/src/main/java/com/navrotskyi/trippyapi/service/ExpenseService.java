package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.dto.trip.ExpenseRequest;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class ExpenseService {

    private final TripNodeRepository tripNodeRepository;
    private final TripEventRepository tripEventRepository;
    private final UserRepository userRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public ExpenseService(TripNodeRepository tripNodeRepository, 
                          TripEventRepository tripEventRepository, 
                          UserRepository userRepository,
                          TripParticipantRepository tripParticipantRepository) {
        this.tripNodeRepository = tripNodeRepository;
        this.tripEventRepository = tripEventRepository;
        this.userRepository = userRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    @Transactional
    public TripNode createExpenseNode(ExpenseRequest request, String reporterEmail) {
        TripEvent trip = tripEventRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wycieczki o podanym ID"));

        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika (reportera) o podanym adresie email"));

        validateUserParticipation(reporter, trip);

        TripNode expenseNode = new TripNode();
        expenseNode.setEvent(trip);
        expenseNode.setName(request.title());
        expenseNode.setStartTime(LocalDateTime.now());
        expenseNode.setEndTime(LocalDateTime.now());
        expenseNode.setPrice(request.price());
        // Flaga istotna dla przyszłego algorytmu bilansującego
        expenseNode.setSeparate(request.isSeparate()); 
        expenseNode.setReporter(reporter);

        return tripNodeRepository.save(expenseNode);
    }

    @Transactional
    public TripNode assignExpenseToNode(UUID nodeId, ExpenseRequest request, String reporterEmail) {
        TripNode existingNode = tripNodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono węzła o podanym ID"));
                
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika (reportera) o podanym adresie email"));

        validateUserParticipation(reporter, existingNode.getEvent());

        existingNode.setPrice(request.price());
        existingNode.setSeparate(request.isSeparate());
        existingNode.setReporter(reporter);
        
        return tripNodeRepository.save(existingNode);
    }

    @Transactional(readOnly = true)
    public List<TripNode> getExpensesByTrip(UUID tripId, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o podanym adresie email"));

        TripEvent trip = tripEventRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wycieczki o podanym ID"));

        validateUserParticipation(reporter, trip);

        return tripNodeRepository.findByEvent(trip);
    }

    private void validateUserParticipation(User user, TripEvent trip) {
        TripParticipant participant = tripParticipantRepository.findByEventAndUser(trip, user)
                .orElseThrow(() -> new AccessDeniedException("Brak uprawnień: użytkownik nie jest przypisany do tej wycieczki."));
        
        if (!participant.isAccepted()) {
            throw new AccessDeniedException("Brak uprawnień: użytkownik nie zaakceptował zaproszenia na tę wycieczkę.");
        }
    }
}
