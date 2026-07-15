package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Timing and dimensions for an entity's current melee attack. */
public class AttackComponent implements Component {
    public float activeTimeRemaining;
    public float cooldownRemaining;
    public float activeDuration = 0.14f;
    public float cooldownDuration = 0.32f;
    public float reach = 1.35f;
    public float halfWidth = 0.42f;

    public boolean isActive() {
        return activeTimeRemaining > 0f;
    }
}
