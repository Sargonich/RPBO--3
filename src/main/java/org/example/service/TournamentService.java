package org.example.service;

import org.example.annotation.Cumponent;
import org.example.dto.TournamentRequest;
import org.example.factory.TournamentFactory;
import org.example.model.Tournament;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Cumponent
@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TeamApplicationRepository applicationRepository;
    private final MatchRepository matchRepository;
    private final TournamentFactory tournamentFactory = new TournamentFactory();

    public TournamentService(
            TournamentRepository tournamentRepository,
            TeamApplicationRepository applicationRepository,
            MatchRepository matchRepository
    ) {
        this.tournamentRepository = tournamentRepository;
        this.applicationRepository = applicationRepository;
        this.matchRepository = matchRepository;
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Турнир не найден: " + id));
    }

    public Tournament createTournament(TournamentRequest request) {
        validateRequest(request);

        Tournament tournament = tournamentFactory.create(
                request.getName(),
                request.getTeamLimit(),
                "PLANNED"
        );

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament updateTournament(Long id, TournamentRequest request) {
        validateRequest(request);

        Tournament tournament = tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Турнир не найден: " + id));

        validateTournamentEditable(tournament);
        validateLimitAgainstApproved(tournament, request.getTeamLimit());
        tournament.setName(request.getName());
        tournament.setTeamLimit(request.getTeamLimit());

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament patchTournament(Long id, TournamentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        Tournament tournament = tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Турнир не найден: " + id));

        validateTournamentEditable(tournament);
        if (request.getName() != null) {
            validateName(request.getName());
            tournament.setName(request.getName());
        }

        if (request.getTeamLimit() != null) {
            validateTeamLimit(request.getTeamLimit());
            validateLimitAgainstApproved(tournament, request.getTeamLimit());
            tournament.setTeamLimit(request.getTeamLimit());
        }

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public void deleteTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Турнир не найден: " + id));

        boolean hasApplications = applicationRepository.existsByTournament_Id(id);

        if (hasApplications) {
            throw new IllegalArgumentException(
                    "Нельзя удалить турнир: существуют связанные заявки");
        }

        boolean hasMatches = matchRepository.existsByTournament_Id(id);

        if (hasMatches) {
            throw new IllegalArgumentException(
                    "Нельзя удалить турнир: существуют связанные матчи");
        }

        tournamentRepository.delete(tournament);
    }


    @Transactional
    public Tournament startTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Турнир не найден: " + id));
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Начать можно только запланированный турнир");
        }
        long approved = applicationRepository.countByTournament_IdAndAdmissionStatus(id, "APPROVED");
        if (approved < 2) {
            throw new IllegalArgumentException("Для начала турнира нужны минимум две допущенные команды");
        }
        tournament.setStatus("IN_PROGRESS");
        return tournamentRepository.save(tournament);
    }

    private void validateTournamentEditable(Tournament tournament) {
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Изменять можно только запланированный турнир");
        }
    }

    private void validateLimitAgainstApproved(Tournament tournament, Integer newLimit) {
        long approved = applicationRepository.countByTournament_IdAndAdmissionStatus(tournament.getId(), "APPROVED");
        if (newLimit != null && newLimit < approved) {
            throw new IllegalArgumentException("Новый лимит не может быть меньше количества допущенных команд: " + approved);
        }
    }

    private void validateRequest(TournamentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        validateName(request.getName());

        if (request.getTeamLimit() == null) {
            throw new IllegalArgumentException(
                    "Необходимо указать teamLimit");
        }

        validateTeamLimit(request.getTeamLimit());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Название турнира не может быть пустым");
        }
    }

    private void validateTeamLimit(Integer teamLimit) {
        if (teamLimit == null || teamLimit <= 0) {
            throw new IllegalArgumentException(
                    "Ограничение количества команд должно быть больше нуля");
        }
    }
}