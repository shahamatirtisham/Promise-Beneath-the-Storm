package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State for an attackable barrel that produces a short area explosion. */
public class ExplosiveBarrelComponent implements Component {
    /** 64 frames at 0.03 seconds per frame. */
    public static final float EXPLOSION_VISUAL_DURATION = 64f * 0.03f;

    public float health = 30f;
    public float explosionRadius = 2.2f;
    public float explosionDamage = 35f;
    public float explosionTimeRemaining;
    public float fuseTimeRemaining;
    public boolean destroyed;
    public boolean explosionApplied;
    public int lastPlayerAttackId = -1;
}
