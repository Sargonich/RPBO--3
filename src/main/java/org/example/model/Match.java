package org.example.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "matches")
@Check(constraints = "score1 >= 0 AND score2 >= 0 AND team1_id <> team2_id AND status IN ('SCHEDULED', 'FINISHED')")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;
    @Column(nullable = false)
    private int score1;
    @Column(nullable = false)
    private int score2;
    @Column(nullable = false, length = 30)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public int getScore1() {
        return score1;
    }

    public void setScore1(int score1) {
        this.score1 = score1;
    }

    public int getScore2() {
        return score2;
    }

    public void setScore2(int score2) {
        this.score2 = score2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}