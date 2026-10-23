package org.example.dto;

public class TournamentRequest {

    private String name;
    private Integer teamLimit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTeamLimit() {
        return teamLimit;
    }

    public void setTeamLimit(Integer teamLimit) {
        this.teamLimit = teamLimit;
    }
}