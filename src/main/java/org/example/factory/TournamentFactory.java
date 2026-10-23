package org.example.factory;

import org.example.model.Tournament;

public class TournamentFactory {

    public Tournament create(String name, int teamLimit, String status) {
        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setTeamLimit(teamLimit);
        tournament.setStatus(status);
        return tournament;
    }
}