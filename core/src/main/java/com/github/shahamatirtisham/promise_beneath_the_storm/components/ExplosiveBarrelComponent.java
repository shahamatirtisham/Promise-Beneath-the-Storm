package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State for an attackable barrel that produces a short area explosion. */
public class ExplosiveBarrelComponent implements Component {
    /** explosion-b: source frames 1-12, skipping the blank first cell. */
    public static final int EXPLOSION_VISIBLE_FRAME_COUNT = 12;
    public static final float EXPLOSION_FRAME_DURATION = 0.07f;
    public static final float EXPLOSION_VISUAL_DURATION =
        EXPLOSION_VISIBLE_FRAME_COUNT * EXPLOSION_FRAME_DURATION;

    public float health = 30f;
    public float explosionRadius = 2.2f;
    public float explosionDamage = 35f;
    public float explosionTimeRemaining;
    public float fuseTimeRemaining;
    public boolean destroyed;
    public boolean explosionApplied;
    public int lastPlayerAttackId = -1;
    public int roomIndex = -1;
    public int spawnIndex = -1;
}
