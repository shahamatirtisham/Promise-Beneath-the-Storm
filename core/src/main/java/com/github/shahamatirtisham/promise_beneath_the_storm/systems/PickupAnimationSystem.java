package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PickupAnimationComponent;

/** Advances independent playback state for entities using shared pickup animations. */
public class PickupAnimationSystem extends IteratingSystem {
    public PickupAnimationSystem() {
        super(Family.all(PickupAnimationComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.getComponent(PickupAnimationComponent.class).stateTime += deltaTime;
    }
}
