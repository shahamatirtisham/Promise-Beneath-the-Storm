package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RangedEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Maintains firing distance while making ranged enemies reposition and strafe. */
public class RangedMovementSystem extends IteratingSystem {
    private final Entity player;

    public RangedMovementSystem(Entity player) {
        super(Family.all(
            RangedEnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            VelocityComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead || playerState.controlsLocked
            || ai.state == EnemyAIComponent.State.DEAD) {
            return;
        }

        if (ai.state == EnemyAIComponent.State.STUNNED) {
            ai.stateTimeRemaining -= deltaTime;
            if (ai.stateTimeRemaining <= 0f) {
                ai.state = EnemyAIComponent.State.CHASE;
            }
            return;
        }

        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        RangedEnemyComponent ranged = enemy.getComponent(RangedEnemyComponent.class);
        float deltaX = playerPosition.x - enemyPosition.x;
        float deltaY = playerPosition.y - enemyPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;
        if (distanceSquared == 0f) {
            return;
        }

        float distance = (float) Math.sqrt(distanceSquared);
        float directionX = deltaX / distance;
        float directionY = deltaY / distance;
        ai.state = EnemyAIComponent.State.CHASE;

        if (distance < ranged.preferredMinimumRange) {
            velocity.vx = -directionX * ai.movementSpeed;
            velocity.vy = -directionY * ai.movementSpeed;
            return;
        }
        if (distance > ranged.preferredMaximumRange) {
            velocity.vx = directionX * ai.movementSpeed;
            velocity.vy = directionY * ai.movementSpeed;
            return;
        }

        ranged.strafeTimeRemaining -= deltaTime;
        if (ranged.strafeTimeRemaining <= 0f) {
            ranged.strafeDirection *= -1f;
            ranged.strafeTimeRemaining = 1.25f;
        }
        float strafeSpeed = ai.movementSpeed * 0.7f;
        velocity.vx = -directionY * ranged.strafeDirection * strafeSpeed;
        velocity.vy = directionX * ranged.strafeDirection * strafeSpeed;
    }
}
