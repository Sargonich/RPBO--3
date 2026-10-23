package org.example.factory;

import org.example.model.Team;
import org.example.model.User;

public class TeamFactory {

    public Team create(String name, User captain) {
        Team team = new Team();
        team.setName(name);
        team.setCaptain(captain);
        return team;
    }
}