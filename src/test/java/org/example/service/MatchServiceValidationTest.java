package org.example.service;

import org.example.dto.MatchRequest;
import org.example.model.Match;
import org.example.model.Team;
import org.example.model.Tournament;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.TournamentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MatchServiceValidationTest {

    private final MatchRepository matches = mock(MatchRepository.class);
    private final TeamRepository teams = mock(TeamRepository.class);
    private final TournamentRepository tournaments = mock(TournamentRepository.class);
    private final TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
    private final MatchService service = new MatchService(matches, teams, tournaments, applications);

    @Test
    void crudCannotCreateFinishedMatchDirectly() {
        MatchRequest request = request("FINISHED");
        assertThrows(IllegalArgumentException.class, () -> service.createMatch(request));
        verifyNoInteractions(tournaments, teams, applications, matches);
    }

    @Test
    void cannotCreateMatchWhenOneTeamIsNotApprovedForTournament() {
        Tournament tournament = tournament("PLANNED");
        Team team1 = team(10L);
        Team team2 = team(20L);
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(teams.findByIdForUpdate(10L)).thenReturn(Optional.of(team1));
        when(teams.findByIdForUpdate(20L)).thenReturn(Optional.of(team2));
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 10L, "APPROVED"))
                .thenReturn(true);
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 20L, "APPROVED"))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.createMatch(request("SCHEDULED")));
        verify(matches, never()).save(any());
    }

    @Test
    void createsMatchWhenBothTeamsAreApprovedForSameTournament() {
        Tournament tournament = tournament("PLANNED");
        Team team1 = team(10L);
        Team team2 = team(20L);
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(teams.findByIdForUpdate(10L)).thenReturn(Optional.of(team1));
        when(teams.findByIdForUpdate(20L)).thenReturn(Optional.of(team2));
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 10L, "APPROVED"))
                .thenReturn(true);
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 20L, "APPROVED"))
                .thenReturn(true);
        when(matches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createMatch(request("SCHEDULED"));

        assertEquals("SCHEDULED", result.getStatus());
        assertEquals(1L, result.getTournament().getId());
        verify(matches).save(any());
    }

    @Test
    void cannotUpdateMatchToUseTeamNotApprovedForTournament() {
        Tournament tournament = tournament("PLANNED");
        Team team1 = team(10L);
        Team team2 = team(20L);
        Match existing = new Match();
        existing.setId(5L);
        existing.setTournament(tournament);
        existing.setTeam1(team1);
        existing.setTeam2(team2);
        existing.setStatus("SCHEDULED");
        when(matches.findByIdForUpdate(5L)).thenReturn(Optional.of(existing));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(teams.findByIdForUpdate(10L)).thenReturn(Optional.of(team1));
        when(teams.findByIdForUpdate(20L)).thenReturn(Optional.of(team2));
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 10L, "APPROVED"))
                .thenReturn(true);
        when(applications.existsByTournament_IdAndTeam_IdAndAdmissionStatus(1L, 20L, "APPROVED"))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.updateMatch(5L, request("SCHEDULED")));
        verify(matches, never()).save(any());
    }

    @Test
    void cannotCreateMatchForTournamentThatAlreadyStarted() {
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament("IN_PROGRESS")));
        assertThrows(IllegalArgumentException.class, () -> service.createMatch(request("SCHEDULED")));
        verifyNoInteractions(teams, applications, matches);
    }

    private static MatchRequest request(String status) {
        MatchRequest request = new MatchRequest();
        request.setTournamentId(1L);
        request.setTeam1Id(10L);
        request.setTeam2Id(20L);
        request.setScore1(0);
        request.setScore2(0);
        request.setStatus(status);
        return request;
    }

    private static Tournament tournament(String status) {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test tournament");
        tournament.setTeamLimit(4);
        tournament.setStatus(status);
        return tournament;
    }

    private static Team team(Long id) {
        Team team = new Team();
        team.setId(id);
        team.setName("Team " + id);
        return team;
    }
}
