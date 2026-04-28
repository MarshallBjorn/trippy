package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.CreateTripNodeRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service zarządzający węzłami wycieczki ({@link TripNode}).
 * <p>
 * Reguły dostępu są spójne dla wszystkich operacji:
 * <ul>
 *   <li><b>Odczyt</b> (lista, pojedynczy node) — wymaga bycia <i>zaakceptowanym</i>
 *       uczestnikiem wycieczki ({@code isAccepted=true}). Zaproszeni, ale nieaktywowani
 *       uczestnicy nie mają dostępu.</li>
 *   <li><b>Tworzenie</b> — wymaga bycia zaakceptowanym uczestnikiem.</li>
 *   <li><b>Aktualizacja / usuwanie</b> — wymaga bycia <i>autorem</i> węzła
 *       <b>lub</b> ORGANIZATOREM wycieczki. W obu przypadkach wykonujący
 *       musi być zaakceptowanym uczestnikiem.</li>
 * </ul>
 * <p>
 * Wszystkie metody zapisujące/modyfikujące zwracają w pełni zainicjalizowaną encję
 * (z załadowanymi relacjami {@code event} i {@code reporter}), tak aby mapper
 * mógł bezpiecznie skonwertować je na DTO bez {@code LazyInitializationException}.
 */
@Service
public class TripNodeService {

    private static final String ROLE_ORGANIZER = "ORGANIZER";

    private final TripNodeRepository tripNodeRepository;
    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public TripNodeService(TripNodeRepository tripNodeRepository,
                           TripEventRepository tripEventRepository,
                           TripParticipantRepository tripParticipantRepository) {
        this.tripNodeRepository = tripNodeRepository;
        this.tripEventRepository = tripEventRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    // ============================================================
    //  CREATE
    // ============================================================

    /**
     * Tworzy nowy węzeł w obrębie podanej wycieczki.
     *
     * @param eventId  identyfikator wycieczki
     * @param request  dane węzła (już zwalidowane przez Bean Validation w kontrolerze)
     * @param reporter aktualnie zalogowany użytkownik (autor węzła)
     * @return świeżo zapisany węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy wycieczka nie istnieje
     * @throws SecurityException         gdy użytkownik nie jest zaakceptowanym uczestnikiem
     * @throws IllegalArgumentException  gdy {@code endTime <= startTime}
     */
    @Transactional
    public TripNode createTripNode(UUID eventId, CreateTripNodeRequest request, User reporter) {
        validateTimeRange(request);

        TripEvent event = tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono wycieczki o id: " + eventId));

        requireAcceptedParticipant(eventId, reporter.getId());

        TripNode node = new TripNode();
        node.setEvent(event);
        node.setReporter(reporter);
        node.setName(request.getName());
        node.setStartTime(request.getStartTime());
        node.setEndTime(request.getEndTime());
        node.setNote(request.getNote());
        node.setPrice(request.getPrice());
        node.setSeparate(request.isSeparate());

        TripNode saved = tripNodeRepository.save(node);

        // Pobieramy zapisaną encję z załadowanymi relacjami przez @EntityGraph,
        // dzięki czemu mapper nie napotka LazyInitializationException.
        return tripNodeRepository.findWithDetailsById(saved.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Właśnie utworzony node zniknął z bazy — to nie powinno się zdarzyć."));
    }

    // ============================================================
    //  READ
    // ============================================================

    /**
     * Zwraca wszystkie węzły wycieczki posortowane po {@code startTime} rosnąco.
     *
     * @param eventId identyfikator wycieczki
     * @param user    aktualnie zalogowany użytkownik
     * @return lista węzłów z załadowanymi relacjami
     * @throws SecurityException gdy użytkownik nie jest zaakceptowanym uczestnikiem
     */
    @Transactional(readOnly = true)
    public List<TripNode> getNodesForEvent(UUID eventId, User user) {
        requireAcceptedParticipant(eventId, user.getId());
        return tripNodeRepository.findAllByEventIdOrderByStartTimeAsc(eventId);
    }

    /**
     * Zwraca pojedynczy węzeł po jego ID, weryfikując że należy do podanej wycieczki.
     *
     * @param eventId identyfikator wycieczki (z URL — sprawdzamy spójność)
     * @param nodeId  identyfikator węzła
     * @param user    aktualnie zalogowany użytkownik
     * @return węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do podanej wycieczki
     * @throws SecurityException         gdy użytkownik nie jest zaakceptowanym uczestnikiem
     */
    @Transactional(readOnly = true)
    public TripNode getTripNode(UUID eventId, UUID nodeId, User user) {
        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        if (!node.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException(
                    "Węzeł " + nodeId + " nie należy do wycieczki " + eventId + ".");
        }

        requireAcceptedParticipant(eventId, user.getId());
        return node;
    }

    // ============================================================
    //  UPDATE
    // ============================================================

    /**
     * Aktualizuje wszystkie pola istniejącego węzła (semantyka PUT — pełna podmiana).
     *
     * @param eventId     identyfikator wycieczki
     * @param nodeId      identyfikator węzła
     * @param request     nowe dane węzła
     * @param currentUser aktualnie zalogowany użytkownik
     * @return zaktualizowany węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do wycieczki lub czasy są niespójne
     * @throws SecurityException         gdy użytkownik nie jest autorem ani organizatorem
     */
    @Transactional
    public TripNode updateTripNode(UUID eventId, UUID nodeId,
                                   CreateTripNodeRequest request, User currentUser) {
        validateTimeRange(request);

        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        validateBelongsToTrip(eventId, node);
        requireAuthorOrOrganizer(eventId, node, currentUser);

        node.setName(request.getName());
        node.setStartTime(request.getStartTime());
        node.setEndTime(request.getEndTime());
        node.setNote(request.getNote());
        node.setPrice(request.getPrice());
        node.setSeparate(request.isSeparate());

        // Hibernate flush'uje przy zakończeniu transakcji; zwracamy tę samą instancję
        // (z już załadowanymi relacjami z findWithDetailsById).
        return tripNodeRepository.save(node);
    }

    // ============================================================
    //  DELETE
    // ============================================================

    /**
     * Usuwa węzeł.
     *
     * @param eventId     identyfikator wycieczki
     * @param nodeId      identyfikator węzła
     * @param currentUser aktualnie zalogowany użytkownik
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do wycieczki
     * @throws SecurityException         gdy użytkownik nie jest autorem ani organizatorem
     */
    @Transactional
    public void deleteTripNode(UUID eventId, UUID nodeId, User currentUser) {
        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        validateBelongsToTrip(eventId, node);
        requireAuthorOrOrganizer(eventId, node, currentUser);

        tripNodeRepository.delete(node);
    }

    // ============================================================
    //  PRIVATE HELPERS — autoryzacja i walidacje
    // ============================================================

    /**
     * Sprawdza, czy użytkownik jest zaakceptowanym uczestnikiem wycieczki.
     * Zaproszeni, ale nieaktywowani ({@code isAccepted=false}) są blokowani.
     */
    private TripParticipant requireAcceptedParticipant(UUID eventId, UUID userId) {
        TripParticipant participant = tripParticipantRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new SecurityException(
                        "Nie jesteś uczestnikiem tej wycieczki."));

        if (!participant.isAccepted()) {
            throw new SecurityException(
                    "Nie zaakceptowałeś jeszcze zaproszenia do tej wycieczki.");
        }
        return participant;
    }

    /**
     * Sprawdza, czy podany użytkownik może modyfikować/usuwać węzeł:
     * musi być zaakceptowanym uczestnikiem oraz albo autorem węzła,
     * albo organizatorem wycieczki.
     */
    private void requireAuthorOrOrganizer(UUID eventId, TripNode node, User currentUser) {
        TripParticipant participant = requireAcceptedParticipant(eventId, currentUser.getId());

        boolean isAuthor = node.getReporter().getId().equals(currentUser.getId());
        boolean isOrganizer = ROLE_ORGANIZER.equalsIgnoreCase(participant.getTripRole().getName());

        if (!isAuthor && !isOrganizer) {
            throw new SecurityException(
                    "Brak uprawnień. Musisz być autorem węzła lub organizatorem wycieczki.");
        }
    }

    /**
     * Sprawdza, że węzeł rzeczywiście należy do wycieczki podanej w URL.
     * Chroni przed sytuacją, w której ktoś podaje {@code /api/trips/A/nodes/X},
     * gdzie {@code X} należy do wycieczki {@code B}.
     */
    private void validateBelongsToTrip(UUID eventId, TripNode node) {
        if (!node.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException(
                    "Węzeł " + node.getId() + " nie należy do wycieczki " + eventId + ".");
        }
    }

    /**
     * Cross-field walidacja: {@code endTime} musi być po {@code startTime}.
     * Bean Validation nie pokrywa walidacji wzajemnej dwóch pól bez customowych adnotacji,
     * więc robimy to ręcznie tutaj.
     */
    private void validateTimeRange(CreateTripNodeRequest request) {
        if (request.getEndTime() == null || request.getStartTime() == null) {
            // Tu już złapie @NotNull z DTO — defensive check.
            return;
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException(
                    "Pole endTime musi być późniejsze niż startTime.");
        }
    }
}