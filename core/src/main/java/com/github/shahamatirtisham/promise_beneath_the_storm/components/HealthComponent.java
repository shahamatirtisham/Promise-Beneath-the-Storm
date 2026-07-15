package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Mutable health data shared by players, enemies, and bosses. */
public class HealthComponent implements Component {
    public float current;
    public float maximum;

    public HealthComponent(float maximum) {
        this.maximum = maximum;
        this.current = maximum;
    }
}
