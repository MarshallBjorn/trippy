package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder {

    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Tabela: currency
    public List<Currency> getSampleCurrencies() {
        return List.of(
                new Currency("PLN", "Polski Złoty"),
                new Currency("USD", "Dolar Amerykański"),
                new Currency("EUR", "Euro"),
                new Currency("GBP", "Funt Brytyjski"),
                new Currency("CHF", "Frank Szwajcarski")
        );
    }


    // Tabela: trip_role
    public List<TripRole> getSampleTripRoles() {
        return List.of(
                new TripRole("ORGANIZER"),
                new TripRole("PARTICIPANT"),
                new TripRole("VIEWER")
        );
    }


    // Tabela: users
    public List<User> getSampleUsers() {
        String pass = passwordEncoder.encode("password");
        return List.of(
                new User("Admin Trippy", "admin@trippy.pl", pass, Role.ADMIN, null, true, false),
                new User("Jan Kowalski", "jan@trippy.pl", pass, Role.USER, null, true, false),
                new User("Anna Nowak", "anna@trippy.pl", pass, Role.USER, null, true, false),
                new User("Marek Mostowiak", "marek@trippy.pl", pass, Role.USER, null, true, false),
                new User("Zofia Kruk", "zofia@trippy.pl", pass, Role.USER, null, true, false),
                new User("Piotr Fronczewski", "piotr@trippy.pl", pass, Role.USER, null, true, false),
                new User("Katarzyna Zielińska", "kasia@trippy.pl", pass, Role.USER, null, true, false),
                new User("Tomasz Wójcik", "tomek@trippy.pl", pass, Role.USER, null, true, false),
                new User("Magdalena Kamińska", "magda@trippy.pl", pass, Role.USER, null, true, false),
                new User("Michał Lewandowski", "michal@trippy.pl", pass, Role.USER, null, true, false)
        );
    }


    // Tabela: trip_event
    public List<TripEvent> getSampleTripEvents(List<User> users, List<Currency> currencies) {
        List<TripEvent> trips = new ArrayList<>();

        //wlasciciele wycieczki
        User jan = users.get(1);
        User anna = users.get(2);
        User marek = users.get(3);
        User zofia = users.get(4);
        User piotr = users.get(5);
        User kasia = users.get(6);
        User tomek = users.get(7);
        User magda = users.get(8);

        Currency pln = currencies.get(0);
        Currency usd = currencies.get(1);
        Currency eur = currencies.get(2);
        Currency gbp = currencies.get(3);

        trips.add(createTrip("Weekend w Tatrach", jan, pln, 10, 14, "1500.00"));
        trips.add(createTrip("Rzymskie Wakacje", anna, eur, 20, 27, "4500.00"));
        trips.add(createTrip("Roadtrip po USA", marek, usd, 40, 60, "15000.00"));
        trips.add(createTrip("Majówka na Mazurach", zofia, pln, -10, -5, "1200.00"));
        trips.add(createTrip("City Break Paryż", piotr, eur, 15, 18, "3000.00"));
        trips.add(createTrip("Narty w Alpach", kasia, eur, 100, 107, "5000.00"));
        trips.add(createTrip("Zwiedzanie Londynu", tomek, gbp, 30, 35, "4000.00"));
        trips.add(createTrip("Kemping Bieszczady", magda, pln, 5, 8, "800.00"));
        trips.add(createTrip("Odpoczynek na Malediwach", marek, usd, 200, 214, "25000.00"));
        trips.add(createTrip("Festiwal w Berlinie", jan, eur, 12, 15, "1800.00"));
        trips.add(createTrip("Praga ze znajomymi", anna, pln, -30, -27, "1000.00"));
        trips.add(createTrip("Sylwester w Wiedniu", kasia, eur, 250, 255, "3500.00"));
        trips.add(createTrip("Smaki Barcelony", piotr, eur, 45, 52, "4200.00"));
        trips.add(createTrip("Słoneczna Lizbona", tomek, eur, 60, 67, "3800.00"));
        trips.add(createTrip("Trekking w Norwegii", magda, eur, 80, 90, "6000.00"));

        return trips;
    }
    private TripEvent createTrip(String name, User owner, Currency currency, int startDaysFromNow, int endDaysFromNow, String budget) {
        TripEvent event = new TripEvent();
        event.setName(name);
        event.setOwner(owner);
        event.setCurrency(currency);
        event.setStartDate(LocalDate.now().plusDays(startDaysFromNow));
        event.setEndDate(LocalDate.now().plusDays(endDaysFromNow));
        event.setBudget(new BigDecimal(budget));
        event.setParticipants(new ArrayList<>());
        return event;
    }


    // Tabela: trip_participant
    public List<TripParticipant> getSampleParticipants(List<TripEvent> trips, List<User> users, List<TripRole> roles) {
        List<TripParticipant> participants = new ArrayList<>();

        TripRole organizer = roles.get(0);
        TripRole participant = roles.get(1);
        TripRole viewer = roles.get(2);


        TripEvent tatry = trips.get(0);
        participants.add(new TripParticipant(tatry, users.get(1), organizer, new BigDecimal("0"), true));
        participants.add(new TripParticipant(tatry, users.get(2), participant, new BigDecimal("500"), true));
        participants.add(new TripParticipant(tatry, users.get(3), participant, new BigDecimal("0"), false));
        participants.add(new TripParticipant(tatry, users.get(4), viewer, new BigDecimal("0"), true));


        TripEvent rzym = trips.get(1);
        participants.add(new TripParticipant(rzym, users.get(2), organizer, new BigDecimal("0"), true));
        participants.add(new TripParticipant(rzym, users.get(1), participant, new BigDecimal("1000"), true));
        participants.add(new TripParticipant(rzym, users.get(5), participant, new BigDecimal("1500"), true));


        TripEvent usa = trips.get(2);
        participants.add(new TripParticipant(usa, users.get(3), organizer, new BigDecimal("0"), true));
        participants.add(new TripParticipant(usa, users.get(6), participant, new BigDecimal("3000"), true));
        participants.add(new TripParticipant(usa, users.get(7), participant, new BigDecimal("2500"), true));


        TripEvent mazury = trips.get(3);
        participants.add(new TripParticipant(mazury, users.get(4), organizer, new BigDecimal("0"), true));
        participants.add(new TripParticipant(mazury, users.get(8), participant, new BigDecimal("200"), true));
        participants.add(new TripParticipant(mazury, users.get(9), participant, new BigDecimal("400"), true));


        TripEvent paryz = trips.get(4);
        participants.add(new TripParticipant(paryz, users.get(5), organizer, new BigDecimal("0"), true));
        participants.add(new TripParticipant(paryz, users.get(1), participant, new BigDecimal("800"), true));
        participants.add(new TripParticipant(paryz, users.get(2), participant, new BigDecimal("0"), false));
        participants.add(new TripParticipant(paryz, users.get(0), viewer, new BigDecimal("0"), true));

        return participants;
    }


    // Tabela: trip_node
    public List<TripNode> getSampleTripNodes(List<TripEvent> trips, List<User> users) {
        List<TripNode> nodes = new ArrayList<>();

        TripEvent tatry = trips.get(0);
        TripEvent rzym = trips.get(1);
        TripEvent usa = trips.get(2);
        TripEvent mazury = trips.get(3);
        TripEvent paryz = trips.get(4);

        User jan = users.get(1);
        User anna = users.get(2);
        User marek = users.get(3);
        User zofia = users.get(4);
        User piotr = users.get(5);
        User kasia = users.get(6);
        User tomek = users.get(7);
        User michal = users.get(9);


        nodes.add(createNode(tatry, jan, "Przejazd pociągiem PKP", "Wagon 4, przedział 2. Miejsca od okna.", tatry.getStartDate().atTime(8, 0), tatry.getStartDate().atTime(13, 30), "120.00", false));
        nodes.add(createNode(tatry, jan, "Zameldowanie w Willi", "Pamiętać o opłacie klimatycznej gotówką.", tatry.getStartDate().atTime(14, 0), tatry.getStartDate().atTime(14, 30), "600.00", false));
        nodes.add(createNode(tatry, anna, "Obiad na Krupówkach", "Zarezerwowany stolik na 4 osoby w Karczmie.", tatry.getStartDate().atTime(15, 30), tatry.getStartDate().atTime(17, 0), "250.00", true));
        nodes.add(createNode(tatry, jan, "Wyjście na Morskie Oko", "Zbiórka rano pod busem. Trzeba kupić bilety wstępu do TPN.", tatry.getStartDate().plusDays(1).atTime(7, 0), tatry.getStartDate().plusDays(1).atTime(15, 0), "40.00", false));
        nodes.add(createNode(tatry, anna, "Wejście na Kasprowy", "Wjazd kolejką, schodzimy pieszo.", tatry.getStartDate().plusDays(2).atTime(9, 0), tatry.getStartDate().plusDays(2).atTime(14, 0), "130.00", false));


        nodes.add(createNode(rzym, anna, "Lot WizzAir", "Odprawa zrobiona, bilety w aplikacji.", rzym.getStartDate().atTime(10, 0), rzym.getStartDate().atTime(12, 15), "450.00", false));
        nodes.add(createNode(rzym, piotr, "Kolacja - prawdziwa Pizza", "Pizzeria polecana przez lokalsów koło hotelu.", rzym.getStartDate().atTime(19, 0), rzym.getStartDate().atTime(21, 0), "180.00", true));
        nodes.add(createNode(rzym, jan, "Zwiedzanie Koloseum", "Bilety kupione online, wejście bez kolejki.", rzym.getStartDate().plusDays(1).atTime(11, 0), rzym.getStartDate().plusDays(1).atTime(14, 0), "200.00", false));


        nodes.add(createNode(usa, marek, "Lot Warszawa - Nowy Jork", "Lot bezpośredni LOT.", usa.getStartDate().atTime(12, 0), usa.getStartDate().atTime(20, 0), "3500.00", false));
        nodes.add(createNode(usa, kasia, "Wypożyczenie auta", "Ford Mustang kabriolet odebrany z lotniska.", usa.getStartDate().plusDays(1).atTime(10, 0), usa.getStartDate().plusDays(1).atTime(11, 0), "4000.00", false));
        nodes.add(createNode(usa, tomek, "Motel na Route 66", "Typowy amerykański motel przy drodze.", usa.getStartDate().plusDays(2).atTime(18, 0), usa.getStartDate().plusDays(3).atTime(10, 0), "800.00", true));


        nodes.add(createNode(mazury, zofia, "Odbiór żaglówki", "Port Sztynort, jacht Antila 27.", mazury.getStartDate().atTime(14, 0), mazury.getStartDate().atTime(15, 0), "1500.00", false));
        nodes.add(createNode(mazury, michal, "Ognisko na dzikiej plaży", "Kupiliśmy drewno i prowiant.", mazury.getStartDate().plusDays(1).atTime(20, 0), mazury.getStartDate().plusDays(1).atTime(23, 59), "150.00", true));


        nodes.add(createNode(paryz, piotr, "Wejście na Wieżę Eiffla", "Wjazd windą na sam szczyt.", paryz.getStartDate().plusDays(1).atTime(18, 0), paryz.getStartDate().plusDays(1).atTime(20, 0), "300.00", false));
        nodes.add(createNode(paryz, jan, "Luwr - zwiedzanie", "Trzeba być z samego rana, żeby uniknąć tłumów.", paryz.getStartDate().plusDays(2).atTime(9, 0), paryz.getStartDate().plusDays(2).atTime(15, 0), "250.00", false));

        return nodes;
    }

    private TripNode createNode(TripEvent event, User reporter, String name, String note, LocalDateTime start, LocalDateTime end, String price, boolean separate) {
        TripNode node = new TripNode();
        node.setEvent(event);
        node.setReporter(reporter);
        node.setName(name);
        node.setNote(note);
        node.setStartTime(start);
        node.setEndTime(end);
        node.setPrice(new BigDecimal(price));
        node.setSeparate(separate);
        return node;
    }

    // Tabele: trip_post oraz trip_photo
    public List<TripPost> getSampleTripPosts(List<TripNode> nodes, List<User> users) {
        List<TripPost> posts = new ArrayList<>();

        User jan = users.get(1);
        User anna = users.get(2);
        User marek = users.get(3);
        User zofia = users.get(4);
        User piotr = users.get(5);
        User kasia = users.get(6);
        User tomek = users.get(7);
        User magda = users.get(8);
        User michal = users.get(9);


        posts.add(createPost(nodes.get(0), jan, "Wyruszamy! Pociąg punktualnie. Miejscówki w Warsie zarezerwowane."));
        posts.add(createPost(nodes.get(0), anna, "Nareszcie urlop! Janek, pamiętaj o kawie dla mnie."));
        posts.add(createPost(nodes.get(1), jan, "Pokoje są super. Czysto i blisko do Krupówek. Rozpakowujemy się."));
        posts.add(createPost(nodes.get(2), anna, "Kwaśnica była genialna, ale oscypek z żurawiną wygrywa wszystko. Pękam!"));
        posts.add(createPost(nodes.get(3), jan, "Udało się wejść! Tłumy niesamowite, ale widok z Morskiego Oka na żywo wynagradza trud."));
        posts.add(createPost(nodes.get(4), anna, "Wjazd na Kasprowy na pełnym luzie. Za to wiatr na górze urywa głowę, dobrze że wzięłam czapkę."));


        posts.add(createPost(nodes.get(5), anna, "Siedzimy w samolocie. Kapitan zapowiada świetną pogodę na miejscu."));
        posts.add(createPost(nodes.get(6), piotr, "Ta pizza to arcydzieło. Nigdy w życiu nie jadłem lepszej margherity."));
        posts.add(createPost(nodes.get(6), jan, "Potwierdzam, Piotrek znalazł super miejsce. Włosi zdecydowanie wiedzą, jak robić jedzenie."));
        posts.add(createPost(nodes.get(7), anna, "Koloseum jest potężne. Trudno uwierzyć, że zbudowali to tak dawno temu bez użycia nowoczesnych maszyn."));


        posts.add(createPost(nodes.get(8), marek, "Zaczynamy amerykański sen! Lot trochę długi, ale damy radę."));
        posts.add(createPost(nodes.get(9), kasia, "Mamy to! Czerwony kabriolet. Marek prowadzi pierwszy, ja podziwiam widoki i pilnuję GPS."));
        posts.add(createPost(nodes.get(10), tomek, "Motel wygląda dokładnie jak z filmów na Netflixie. Właściciel bardzo miły, dostaliśmy darmową dolewkę kawy."));


        posts.add(createPost(nodes.get(11), zofia, "Łódź odebrana. Wiatr dzisiaj dopisuje, zaraz wychodzimy z portu i stawiamy żagle."));
        posts.add(createPost(nodes.get(12), michal, "Kiełbasy upieczone, gitara gra. Idealny wieczór na Mazurach."));
        posts.add(createPost(nodes.get(12), magda, "Komary trochę tną, ale klimat ogniska z Wami jest nie do podrobienia."));


        posts.add(createPost(nodes.get(13), piotr, "Widok z Wieży Eiffla nocą to magia. Cały Paryż się świeci, polecam każdemu wjazd po zmroku."));
        posts.add(createPost(nodes.get(14), jan, "Kolejka do Mony Lisy to jakiś żart. Czekałem 40 minut na jedno zdjęcie, ale i tak warto było."));

        return posts;
    }

    private TripPost createPost(TripNode node, User reporter, String note) {
        TripPost post = new TripPost();
        post.setNode(node);
        post.setReporter(reporter);
        post.setNote(note);
        post.setPhotos(new ArrayList<>());
        return post;
    }