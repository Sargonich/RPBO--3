package org.example.factory;

import org.example.model.Match;
import org.example.model.Team;
import org.example.model.Tournament;

public class MatchFactory {

    public Match create(
            Tournament tournament,
            Team team1,
            Team team2,
            int score1,
            int score2,
            String status
    ) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStatus(status);
        return match;
    }
}