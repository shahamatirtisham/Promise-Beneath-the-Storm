package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.SlowZoneComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Applies water slowdown after normal input but before dash movement. */
public class WaterSlowSystem extends EntitySystem {
    private final Entity player;
    private final Array<Entity> zones;

    public WaterSlowSystem(Entity player, Array<Entity> zones) {
        this.player = player;
        this.zones = zones;
    }

    @Override
    public void update(float deltaTime) {
        PositionComponent position = player.getComponent(PositionComponent.class);
        for (Entity zone : zones) {
            SlowZoneComponent slow = zone.getComponent(SlowZoneComponent.class);
            if (!slow.bounds.contains(position.x, position.y)) {
                continue;
            }
            VelocityComponent velocity = player.getComponent(VelocityComponent.class);
            velocity.vx *= slow.speedMultiplier;
            velocity.vy *= slow.speedMultiplier;
            return;
        }
    }
}
