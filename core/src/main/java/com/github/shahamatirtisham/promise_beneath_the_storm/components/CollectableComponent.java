package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** A world pickup that grants Devil Coins when touched by the player. */
public class CollectableComponent implements Component {
    public final int coinValue;
    public boolean collected;

    public CollectableComponent(int coinValue) {
        this.coinValue = coinValue;
    }
}
