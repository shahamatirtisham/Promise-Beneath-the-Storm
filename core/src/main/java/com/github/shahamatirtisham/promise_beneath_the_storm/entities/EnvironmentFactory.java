package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Rectangle;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.SlowZoneComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ExplosiveBarrelComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Creates placeholder environmental mechanics until maps author their positions. */
public final class EnvironmentFactory {
    private EnvironmentFactory() {
    }

    public static Entity createWaterZone(Rectangle bounds) {
        Entity zone = new Entity();
        zone.add(new SlowZoneComponent(bounds, 0.55f));
        return zone;
    }

    public static Entity createExplosiveBarrel(float x, float y) {
        Entity barrel = new Entity();
        barrel.add(new PositionComponent(x, y));
        barrel.add(new ExplosiveBarrelComponent());
        return barrel;
    }
}
