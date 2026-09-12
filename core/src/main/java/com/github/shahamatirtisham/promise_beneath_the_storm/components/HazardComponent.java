package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/** Runtime state for a room hazard. */
public class HazardComponent implements Component {
    public enum Type {
        SPIKES,
        FIRE_VENT,
        FIRE,
        POISON_POOL
    }

    public final Type type;
    public final Rectangle bounds;
    private final Polygon polygon;
    public float cycleTime;
    public boolean active;
    public boolean hitThisCycle;

    public HazardComponent(Type type, Rectangle bounds, float startingTime) {
        this.type = type;
        this.bounds = bounds;
        this.polygon = null;
        this.cycleTime = startingTime;
        this.active = type == Type.POISON_POOL || type == Type.FIRE;
    }

    public HazardComponent(Type type, Polygon polygon, float startingTime) {
        this.type = type;
        this.bounds = polygon.getBoundingRectangle();
        this.polygon = polygon;
        this.cycleTime = startingTime;
        this.active = type == Type.POISON_POOL || type == Type.FIRE;
    }

    public boolean contains(float x, float y) {
        return polygon == null ? bounds.contains(x, y) : polygon.contains(x, y);
    }

    public boolean isWarning() {
        if (type == Type.SPIKES) {
            return cycleTime >= 1.5f && cycleTime < 2.25f;
        }
        if (type == Type.FIRE_VENT) {
            return cycleTime >= 1.8f && cycleTime < 2.7f;
        }
        return false;
    }
}
