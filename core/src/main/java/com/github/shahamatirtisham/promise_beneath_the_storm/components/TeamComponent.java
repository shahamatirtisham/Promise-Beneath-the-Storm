package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Identifies allies and enemies so attacks cannot damage their owner or teammates. */
public class TeamComponent implements Component {
    public enum Team {
        PLAYER,
        ENEMY
    }

    public final Team team;

    public TeamComponent(Team team) {
        this.team = team;
    }
}
