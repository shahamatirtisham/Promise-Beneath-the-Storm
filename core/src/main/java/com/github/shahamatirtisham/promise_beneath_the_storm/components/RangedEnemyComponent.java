package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Shooting configuration for the first ranged-enemy prototype. */
public class RangedEnemyComponent implements Component {
    public float cooldownRemaining = 0.75f;
    public float attackCooldown = 1.5f;
    public float projectileSpeed = 5.5f;
    public float attackRange = 8f;
    public float preferredMinimumRange = 3.2f;
    public float preferredMaximumRange = 5.2f;
    public float strafeDirection = 1f;
    public float strafeTimeRemaining = 1.25f;
}
