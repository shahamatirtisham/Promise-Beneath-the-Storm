package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Damage and lifetime data for a projectile moving through the room. */
public class ProjectileComponent implements Component {
    public static final float IRHOS_REVEALED_EFFECT_FRAME_DURATION = 0.10f;

    public float damage;
    public float lifetimeRemaining;
    public float radius;
    public int statusLevelOverride;
    /** Shared wizard-effect artwork for Devil's Crown and Irhos Revealed volleys. */
    public boolean irhosRevealedEffect;
    public boolean impactVisual;
    public float visualStateTime;

    public ProjectileComponent(float damage, float lifetime, float radius) {
        this(damage, lifetime, radius, 0);
    }

    public ProjectileComponent(
        float damage,
        float lifetime,
        float radius,
        int statusLevelOverride
    ) {
        this.damage = damage;
        this.lifetimeRemaining = lifetime;
        this.radius = radius;
        this.statusLevelOverride = statusLevelOverride;
    }
}
