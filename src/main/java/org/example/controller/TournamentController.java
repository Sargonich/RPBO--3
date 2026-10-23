package org.example.controller;

import org.example.annotation.Cumponent;
import org.example.dto.TournamentRequest;
import org.example.model.Tournament;
import org.example.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Cumponent
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tournament> getTournamentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @PostMapping
    public ResponseEntity<Tournament> createTournament(
            @RequestBody TournamentRequest request
    ) {
        Tournament tournament = tournamentService.createTournament(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tournament> updateTournament(
            @PathVariable Long id,
            @RequestBody TournamentRequest request
    ) {
        return ResponseEntity.ok(
                tournamentService.updateTournament(id, request)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Tournament> patchTournament(
            @PathVariable Long id,
            @RequestBody TournamentRequest request
    ) {
        return ResponseEntity.ok(
                tournamentService.patchTournament(id, request)
        );
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<Tournament> startTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.startTournament(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }
}