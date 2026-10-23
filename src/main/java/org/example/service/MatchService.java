package org.example.service;

import org.example.annotation.Cumponent;
import org.example.dto.MatchRequest;
import org.example.factory.MatchFactory;
import org.example.model.Match;
import org.example.model.Team;
import org.example.model.Tournament;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Cumponent
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamApplicationRepository applicationRepository;
    private final MatchFactory matchFactory = new MatchFactory();

    public MatchService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository,
            TeamApplicationRepository applicationRepository
    ) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Матч не найден: " + id));
    }

    @Transactional
    public Match createMatch(MatchRequest request) {
        validateRequest(request);

        Tournament tournament = findTournamentForUpdate(request.getTournamentId());
        validateMatchEditable(tournament, request.getStatus());
        Team[] lockedTeams = lockTeamsInOrder(request.getTeam1Id(), request.getTeam2Id());
        Team team1 = lockedTeams[0];
        Team team2 = lockedTeams[1];

        validateTeams(team1, team2);
        validateTeamsApprovedForTournament(tournament, team1, team2);

        Match match = matchFactory.create(
                tournament,
                team1,
                team2,
                request.getScore1(),
                request.getScore2(),
                request.getStatus()
        );

        return matchRepository.save(match);
    }

    @Transactional
    public Match updateMatch(Long id, MatchRequest request) {
        validateRequest(request);

        Match match = lockMatch(id);
        ensureMatchNotFinished(match);
        Long oldTournamentId = match.getTournament().getId();
        lockTournamentsInOrder(oldTournamentId, request.getTournamentId());
        Tournament oldTournament = findTournamentForUpdate(oldTournamentId);
        Tournament tournament = findTournamentForUpdate(request.getTournamentId());
        validateMatchEditable(oldTournament, match.getStatus());
        validateMatchEditable(tournament, request.getStatus());
        Team[] lockedTeams = lockTeamsInOrder(request.getTeam1Id(), request.getTeam2Id());
        Team team1 = lockedTeams[0];
        Team team2 = lockedTeams[1];

        validateTeams(team1, team2);
        validateTeamsApprovedForTournament(tournament, team1, team2);

        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(request.getScore1());
        match.setScore2(request.getScore2());
        match.setStatus(request.getStatus());

        return matchRepository.save(match);
    }

    @Transactional
    public Match patchMatch(Long id, MatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        Match match = lockMatch(id);
        ensureMatchNotFinished(match);
        Long oldTournamentId = match.getTournament().getId();
        Long requestedTournamentId = request.getTournamentId() != null
                ? request.getTournamentId() : oldTournamentId;
        lockTournamentsInOrder(oldTournamentId, requestedTournamentId);
        Tournament oldTournament = findTournamentForUpdate(oldTournamentId);
        Tournament tournament = findTournamentForUpdate(requestedTournamentId);
        validateMatchEditable(oldTournament, match.getStatus());

        Long requestedTeam1Id = request.getTeam1Id() != null
                ? request.getTeam1Id() : match.getTeam1().getId();
        Long requestedTeam2Id = request.getTeam2Id() != null
                ? request.getTeam2Id() : match.getTeam2().getId();
        Team[] lockedTeams = lockTeamsInOrder(requestedTeam1Id, requestedTeam2Id);
        Team team1 = lockedTeams[0];
        Team team2 = lockedTeams[1];

        validateTeams(team1, team2);
        validateTeamsApprovedForTournament(tournament, team1, team2);
        validateMatchEditable(tournament, request.getStatus() != null
                ? request.getStatus() : match.getStatus());

        if (request.getScore1() != null) {
            validateScore(request.getScore1());
        }

        if (request.getScore2() != null) {
            validateScore(request.getScore2());
        }

        if (request.getStatus() != null && request.getStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "Статус матча не может быть пустым");
        }

        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);

        if (request.getScore1() != null) {
            match.setScore1(request.getScore1());
        }

        if (request.getScore2() != null) {
            match.setScore2(request.getScore2());
        }

        if (request.getStatus() != null) {
            match.setStatus(request.getStatus());
        }

        return matchRepository.save(match);
    }

    @Transactional
    public void deleteMatch(Long id) {
        Match match = lockMatch(id);
        ensureMatchNotFinished(match);
        Tournament tournament = findTournamentForUpdate(match.getTournament().getId());
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Нельзя удалить матч после начала турнира");
        }
        matchRepository.delete(match);
    }


    @Transactional
    public Match finishMatch(Long id) {
        Match match = lockMatch(id);
        Tournament tournament = findTournamentForUpdate(match.getTournament().getId());
        if (!"IN_PROGRESS".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Завершить матч можно только в идущем турнире");
        }
        if (!"SCHEDULED".equals(match.getStatus())) {
            throw new IllegalArgumentException("Завершить можно только запланированный матч");
        }
        match.setStatus("FINISHED");
        return matchRepository.save(match);
    }

    private Match lockMatch(Long id) {
        return matchRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Матч не найден: " + id));
    }

    private void lockTournamentsInOrder(Long firstId, Long secondId) {
        if (firstId.equals(secondId)) {
            findTournamentForUpdate(firstId);
            return;
        }
        findTournamentForUpdate(Math.min(firstId, secondId));
        findTournamentForUpdate(Math.max(firstId, secondId));
    }

    private Tournament findTournamentForUpdate(Long id) {
        return tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Турнир не найден: " + id));
    }

    private Team[] lockTeamsInOrder(Long firstId, Long secondId) {
        if (firstId.equals(secondId)) {
            Team team = lockTeam(firstId);
            return new Team[]{team, team};
        }
        long low = Math.min(firstId, secondId);
        long high = Math.max(firstId, secondId);
        Team lowTeam = lockTeam(low);
        Team highTeam = lockTeam(high);
        return firstId < secondId
                ? new Team[]{lowTeam, highTeam}
                : new Team[]{highTeam, lowTeam};
    }

    private Team lockTeam(Long id) {
        return teamRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + id));
    }



    private void validateRequest(MatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        if (request.getTournamentId() == null
                || request.getTeam1Id() == null
                || request.getTeam2Id() == null
                || request.getScore1() == null
                || request.getScore2() == null
                || request.getStatus() == null
                || request.getStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "Необходимо заполнить все поля матча");
        }

        validateScore(request.getScore1());
        validateScore(request.getScore2());
        if (!"SCHEDULED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Статус матча через CRUD должен быть SCHEDULED; завершение выполняется бизнес-операцией");
        }
    }

    private void validateMatchEditable(Tournament tournament, String status) {
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Создавать и изменять матчи можно только до начала турнира");
        }
        if (!"SCHEDULED".equals(status)) {
            throw new IllegalArgumentException("Статус матча через CRUD должен быть SCHEDULED; завершение выполняется бизнес-операцией");
        }
    }

    private void ensureMatchNotFinished(Match match) {
        if ("FINISHED".equals(match.getStatus())) {
            throw new IllegalArgumentException("Завершённый матч нельзя изменять через CRUD");
        }
    }

    private void validateScore(Integer score) {
        if (score == null || score < 0) {
            throw new IllegalArgumentException(
                    "Счёт матча не может быть отрицательным");
        }
    }

    private void validateTeamsApprovedForTournament(
            Tournament tournament, Team team1, Team team2) {
        boolean team1Approved = applicationRepository
                .existsByTournament_IdAndTeam_IdAndAdmissionStatus(
                        tournament.getId(), team1.getId(), "APPROVED");
        boolean team2Approved = applicationRepository
                .existsByTournament_IdAndTeam_IdAndAdmissionStatus(
                        tournament.getId(), team2.getId(), "APPROVED");
        if (!team1Approved || !team2Approved) {
            throw new IllegalArgumentException(
                    "Создавать или изменять матч можно только между командами, допущенными к этому турниру");
        }
    }

    private void validateTeams(Team team1, Team team2) {
        if (team1.getId().equals(team2.getId())) {
            throw new IllegalArgumentException(
                    "Одна команда не может играть сама с собой");
        }
    }
}