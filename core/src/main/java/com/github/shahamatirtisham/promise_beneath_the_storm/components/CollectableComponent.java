package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** A world pickup that grants Devil Coins when touched by the player. */
public class CollectableComponent implements Component {
    public enum Type {
        DEVIL_COINS,
        HEAL,
        MAX_HEALTH
    }

    public final Type type;
    public final int value;
    public boolean collected;

    public CollectableComponent(Type type, int value) {
        this.type = type;
        this.value = value;
    }
}
