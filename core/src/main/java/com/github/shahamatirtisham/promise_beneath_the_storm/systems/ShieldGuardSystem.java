package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;

/** Tracks the player while mobile, but locks shield direction during attack commitment. */
public class ShieldGuardSystem extends IteratingSystem {
    private final Entity player;

    public ShieldGuardSystem(Entity player) {
        super(Family.all(
            ShieldGuardComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity guard, float deltaTime) {
        ShieldGuardComponent shield = guard.getComponent(ShieldGuardComponent.class);
        shield.guardBrokenTimeRemaining = Math.max(
            0f,
            shield.guardBrokenTimeRemaining - deltaTime
        );
        EnemyAIComponent ai = guard.getComponent(EnemyAIComponent.class);
        if (ai.state != EnemyAIComponent.State.IDLE
            && ai.state != EnemyAIComponent.State.CHASE) {
            return;
        }
        PositionComponent guardPosition = guard.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - guardPosition.x;
        float deltaY = playerPosition.y - guardPosition.y;
        float lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0f) {
            return;
        }
        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        shield.facingX = deltaX * inverseLength;
        shield.facingY = deltaY * inverseLength;
    }
}
