package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Marks the enemy guaranteed to drop the current room's chest key. */
public class KeyCarrierComponent implements Component {
    public boolean keyDropped;
}
