package org.example.service;

import org.example.dto.TournamentRequest;
import org.example.model.Tournament;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TournamentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TournamentServiceStateTest {
    @Test
    void cannotChangeTeamLimitAfterTournamentStarts() {
        TournamentRepository tournaments = mock(TournamentRepository.class);
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Started");
        tournament.setTeamLimit(4);
        tournament.setStatus("IN_PROGRESS");
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        TournamentService service = new TournamentService(tournaments,
                mock(TeamApplicationRepository.class), mock(MatchRepository.class));
        TournamentRequest request = new TournamentRequest();
        request.setName("Started");
        request.setTeamLimit(2);

        assertThrows(IllegalArgumentException.class, () -> service.updateTournament(1L, request));
        verify(tournaments, never()).save(any());
    }

    @Test
    void cannotStartTournamentWithFewerThanTwoApprovedTeams() {
        TournamentRepository tournaments = mock(TournamentRepository.class);
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Not ready");
        tournament.setTeamLimit(4);
        tournament.setStatus("PLANNED");
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(applications.countByTournament_IdAndAdmissionStatus(1L, "APPROVED")).thenReturn(1L);
        TournamentService service = new TournamentService(tournaments, applications, mock(MatchRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.startTournament(1L));
        verify(tournaments, never()).save(any());
    }
}
