package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

public class MovementSystem extends IteratingSystem {

    private static final Family family = Family.all(
        PositionComponent.class,
        VelocityComponent.class
    ).get();

    public MovementSystem() {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = entity.getComponent(PositionComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        // Debug: print position before movement
        if (velocity.vx != 0 || velocity.vy != 0) {
            System.out.println("Before: pos=(" + position.x + "," + position.y +
                ") vel=(" + velocity.vx + "," + velocity.vy +
                ") delta=" + deltaTime);
        }

        // Apply movement
        position.x += velocity.vx * deltaTime;
        position.y += velocity.vy * deltaTime;

        // Debug: print position after movement
        if (velocity.vx != 0 || velocity.vy != 0) {
            System.out.println("After: pos=(" + position.x + "," + position.y + ")");
        }

        // Don't apply friction — let input control velocity fully
        // (Remove the friction code that was setting velocities to 0)
    }
}
