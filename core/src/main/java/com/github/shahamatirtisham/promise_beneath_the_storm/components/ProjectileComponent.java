package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Damage and lifetime data for a projectile moving through the room. */
public class ProjectileComponent implements Component {
    public float damage;
    public float lifetimeRemaining;
    public float radius;
    public int statusLevelOverride;

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
