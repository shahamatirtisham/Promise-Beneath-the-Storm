package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State and tuning for an enemy that commits to a telegraphed line charge. */
public class ChargerComponent implements Component {
    public enum State {
        APPROACH,
        WINDUP,
        CHARGING,
        STUNNED
    }

    public State state = State.APPROACH;
    public float stateTimeRemaining;
    public float directionX;
    public float directionY;
    public float windupDuration = 0.75f;
    public float chargeDuration = 0.65f;
    public float stunDuration = 0.9f;
    public float chargeSpeed = 8f;
    public float triggerRange = 5.5f;
}
