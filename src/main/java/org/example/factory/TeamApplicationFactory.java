package org.example.factory;

import org.example.model.Team;
import org.example.model.Tournament;
import org.example.model.TeamApplication;

public class TeamApplicationFactory {

    public TeamApplication create(
            Team team,
            Tournament tournament,
            String admissionStatus
    ) {
        TeamApplication application = new TeamApplication();
        application.setTeam(team);
        application.setTournament(tournament);
        application.setAdmissionStatus(admissionStatus);
        return application;
    }
}