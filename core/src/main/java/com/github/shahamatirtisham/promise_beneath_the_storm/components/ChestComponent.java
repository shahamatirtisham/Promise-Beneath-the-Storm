package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State for an interactable reward chest. */
public class ChestComponent implements Component {
    public final int coinValue;
    public boolean unlocked;
    public boolean opened;

    public ChestComponent(int coinValue) {
        this.coinValue = coinValue;
    }
}
