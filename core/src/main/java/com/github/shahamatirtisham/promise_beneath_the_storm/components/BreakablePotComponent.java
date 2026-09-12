package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

public class BreakablePotComponent implements Component {

    public enum State {
        IDLE,
        BREAKING
    }

    public State state = State.IDLE;
    public float stateTime = 0f;
    public int variant = 0;
    public int roomIndex = -1;
    public int spawnIndex = -1;
    public boolean dropSpawned = false;
}
