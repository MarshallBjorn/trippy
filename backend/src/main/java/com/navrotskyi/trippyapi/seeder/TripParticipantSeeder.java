package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripParticipantSeeder {

    public List<TripParticipant> getSampleParticipants(List<TripEvent> trips, List<User> users, List<TripRole> roles) {
        List<TripParticipant> participants = new ArrayList<>();
        TripRole organizer = roles.get(0);
        TripRole participant = roles.get(1);
        TripRole viewer = roles.get(2);

        for (int i = 0; i < trips.size(); i++) {
            TripEvent t = trips.get(i);
            participants.add(new TripParticipant(t, t.getOwner(), organizer, BigDecimal.ZERO, true));

            User pUser = users.get((i + 1) % users.size());
            if (!pUser.equals(t.getOwner())) {
                participants.add(new TripParticipant(t, pUser, participant, new BigDecimal("200"), true));
            }

            User vUser = users.get((i + 2) % users.size());
            if (!vUser.equals(t.getOwner()) && !vUser.equals(pUser)) {
                participants.add(new TripParticipant(t, vUser, viewer, BigDecimal.ZERO, true));
            }
        }
        return participants;
    }
}