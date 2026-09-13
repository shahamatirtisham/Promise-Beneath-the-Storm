package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.SlowZoneComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ExplosiveBarrelComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HazardComponent;

/** Creates placeholder environmental mechanics until maps author their positions. */
public final class EnvironmentFactory {
    private EnvironmentFactory() {
    }

    public static Entity createWaterZone(Rectangle bounds) {
        Entity zone = new Entity();
        zone.add(new SlowZoneComponent(bounds, 0.55f));
        return zone;
    }

    public static Entity createExplosiveBarrel(
        World world,
        Rectangle bounds,
        int roomIndex,
        int spawnIndex
    ) {
        Vector2 center = bounds.getCenter(new Vector2());
        Body body = WorldUtils.createStaticRectangle(world, bounds);
        Entity barrel = new Entity();
        barrel.add(new PositionComponent(center.x, center.y));
        barrel.add(new PhysicsComponent(body));
        ExplosiveBarrelComponent data = new ExplosiveBarrelComponent();
        data.roomIndex = roomIndex;
        data.spawnIndex = spawnIndex;
        barrel.add(data);
        return barrel;
    }

    public static Entity createHazard(
        HazardComponent.Type type,
        Rectangle bounds,
        float startingTime
    ) {
        Entity hazard = new Entity();
        hazard.add(new HazardComponent(type, bounds, startingTime));
        return hazard;
    }

    public static Entity createHazard(
        HazardComponent.Type type,
        Polygon bounds,
        float startingTime
    ) {
        Entity hazard = new Entity();
        hazard.add(new HazardComponent(type, bounds, startingTime));
        return hazard;
    }
}
