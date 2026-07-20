package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Directional defense and temporary guard-break state. */
public class ShieldGuardComponent implements Component {
    public float facingX = -1f;
    public float facingY;
    public float frontalDamageReduction = 0.8f;
    public float guardBrokenTimeRemaining;
    public float comboBreakDuration = 2.5f;
    public float parryBreakDuration = 3.5f;

    public boolean isGuardBroken() {
        return guardBrokenTimeRemaining > 0f;
    }
}
