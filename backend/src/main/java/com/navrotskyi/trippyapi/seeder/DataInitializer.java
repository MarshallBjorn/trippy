package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    // Repozytoria
    private final CurrencyRepository currencyRepository;
    private final TripRoleRepository tripRoleRepository;
    private final UserRepository userRepository;
    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final TripNodeRepository tripNodeRepository;
    private final TripPostRepository tripPostRepository;

    // Seedery
    private final CurrencySeeder currencySeeder;
    private final TripRoleSeeder tripRoleSeeder;
    private final UserSeeder userSeeder;
    private final TripEventSeeder tripEventSeeder;
    private final TripParticipantSeeder tripParticipantSeeder;
    private final TripNodeSeeder tripNodeSeeder;
    private final TripPostSeeder tripPostSeeder;

    public DataInitializer(CurrencyRepository currencyRepository, TripRoleRepository tripRoleRepository,
                           UserRepository userRepository, TripEventRepository tripEventRepository,
                           TripParticipantRepository tripParticipantRepository, TripNodeRepository tripNodeRepository,
                           TripPostRepository tripPostRepository, CurrencySeeder currencySeeder,
                           TripRoleSeeder tripRoleSeeder, UserSeeder userSeeder, TripEventSeeder tripEventSeeder,
                           TripParticipantSeeder tripParticipantSeeder, TripNodeSeeder tripNodeSeeder,
                           TripPostSeeder tripPostSeeder) {
        this.currencyRepository = currencyRepository;
        this.tripRoleRepository = tripRoleRepository;
        this.userRepository = userRepository;
        this.tripEventRepository = tripEventRepository;
        this.tripParticipantRepository = tripParticipantRepository;
        this.tripNodeRepository = tripNodeRepository;
        this.tripPostRepository = tripPostRepository;
        this.currencySeeder = currencySeeder;
        this.tripRoleSeeder = tripRoleSeeder;
        this.userSeeder = userSeeder;
        this.tripEventSeeder = tripEventSeeder;
        this.tripParticipantSeeder = tripParticipantSeeder;
        this.tripNodeSeeder = tripNodeSeeder;
        this.tripPostSeeder = tripPostSeeder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println("[SEEDER] Wykryto pustą bazę. Uruchamianie skryptów seedujących...");

            List<Currency> currencies = currencySeeder.getSampleCurrencies();
            currencyRepository.saveAllAndFlush(currencies);

            List<TripRole> roles = tripRoleSeeder.getSampleTripRoles();
            tripRoleRepository.saveAllAndFlush(roles);

            List<User> users = userSeeder.getSampleUsers();
            userRepository.saveAllAndFlush(users);

            List<TripEvent> trips = tripEventSeeder.getSampleTripEvents(users, currencies);
            tripEventRepository.saveAllAndFlush(trips);

            List<TripParticipant> participants = tripParticipantSeeder.getSampleParticipants(trips, users, roles);
            tripParticipantRepository.saveAllAndFlush(participants);

            List<TripNode> nodes = tripNodeSeeder.getSampleTripNodes(trips, users);
            tripNodeRepository.saveAllAndFlush(nodes);

            List<TripPost> posts = tripPostSeeder.getSampleTripPosts(nodes, users);
            tripPostRepository.saveAllAndFlush(posts);

            System.out.println("[SEEDER] Sukces! Baza została zasilona danymi testowymi.");
        }
    }
}