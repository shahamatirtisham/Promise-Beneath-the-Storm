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
    public float damage = 20f;
    public int attackId;
    public int comboStep = -1;
    public float comboResetRemaining;
    public float comboResetDuration = 0.55f;
    public float knockbackStrength;

    public boolean isActive() {
        return activeTimeRemaining > 0f;
    }
}
