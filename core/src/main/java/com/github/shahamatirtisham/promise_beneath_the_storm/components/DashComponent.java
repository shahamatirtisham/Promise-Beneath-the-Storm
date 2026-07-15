package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Timing, direction, and tuning values for the player's dodge dash. */
public class DashComponent implements Component {
    public float activeTimeRemaining;
    public float cooldownRemaining;
    public float directionX = 1f;
    public float directionY;
    public float activeDuration = 0.16f;
    public float cooldownDuration = 0.9f;
    public float speed = 12f;

    public boolean isActive() {
        return activeTimeRemaining > 0f;
    }
}
