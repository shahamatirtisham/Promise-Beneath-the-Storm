package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Persistent throwable-knife inventory and tuning for the player. */
public class PlayerRangedComponent implements Component {
    public static final int MAXIMUM_POUCH_CAPACITY = 5;

    public int maximumCharges = 1;
    public int charges = 1;
    public float cooldownRemaining;
    public float cooldownDuration = 0.45f;
    public float projectileSpeed = 12f;
    public float damage = 12f;

    public boolean addKnife() {
        if (charges >= maximumCharges) {
            return false;
        }
        charges++;
        return true;
    }

    public boolean upgradePouch() {
        if (maximumCharges >= MAXIMUM_POUCH_CAPACITY) {
            return false;
        }
        maximumCharges++;
        return true;
    }

    public void resetCooldown() {
        cooldownRemaining = 0f;
    }
}
