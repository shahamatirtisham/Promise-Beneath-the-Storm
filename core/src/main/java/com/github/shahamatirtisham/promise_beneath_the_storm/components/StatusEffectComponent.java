package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Timers for temporary harmful effects currently affecting an entity. */
public class StatusEffectComponent implements Component {
    public float burningTime;
    public float poisonTime;
    public float slowTime;
    public float stunTime;
    public float damageTickTime;

    public boolean isSlowed() {
        return slowTime > 0f;
    }

    public boolean isStunned() {
        return stunTime > 0f;
    }

    public void clear() {
        burningTime = 0f;
        poisonTime = 0f;
        slowTime = 0f;
        stunTime = 0f;
        damageTickTime = 0f;
    }
}
