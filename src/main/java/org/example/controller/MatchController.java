package org.example.controller;

import org.example.annotation.Cumponent;
import org.example.dto.MatchRequest;
import org.example.model.Match;
import org.example.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Cumponent
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(
            @RequestBody MatchRequest request
    ) {
        Match match = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Match> updateMatch(
            @PathVariable Long id,
            @RequestBody MatchRequest request
    ) {
        return ResponseEntity.ok(matchService.updateMatch(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Match> patchMatch(
            @PathVariable Long id,
            @RequestBody MatchRequest request
    ) {
        return ResponseEntity.ok(matchService.patchMatch(id, request));
    }


    @PostMapping("/{id}/finish")
    public ResponseEntity<Match> finishMatch(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.finishMatch(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}