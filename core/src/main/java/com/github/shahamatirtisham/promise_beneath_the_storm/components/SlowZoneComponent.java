package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;

/** A rectangular environmental zone that reduces normal movement speed. */
public class SlowZoneComponent implements Component {
    public final Rectangle bounds;
    public final float speedMultiplier;

    public SlowZoneComponent(Rectangle bounds, float speedMultiplier) {
        this.bounds = new Rectangle(bounds);
        this.speedMultiplier = speedMultiplier;
    }
}
