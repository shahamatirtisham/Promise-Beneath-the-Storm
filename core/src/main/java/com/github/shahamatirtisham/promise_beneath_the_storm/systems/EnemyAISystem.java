package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RangedEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChargerComponent;

/** Runs the first enemy's finite-state machine: idle, chase, attack, and recovery. */
public class EnemyAISystem extends IteratingSystem {
    private final Entity player;

    public EnemyAISystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            VelocityComponent.class
        ).exclude(RangedEnemyComponent.class, ChargerComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);

        float deltaX = playerPosition.x - enemyPosition.x;
        float deltaY = playerPosition.y - enemyPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        velocity.vx = 0f;
        velocity.vy = 0f;

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead || playerState.controlsLocked) {
            return;
        }

        switch (ai.state) {
            case IDLE:
                if (distanceSquared <= ai.detectionRange * ai.detectionRange) {
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
            case CHASE:
                if (distanceSquared <= ai.attackRange * ai.attackRange) {
                    enterTimedState(ai, EnemyAIComponent.State.ATTACK, ai.attackWindup);
                } else {
                    moveTowardPlayer(velocity, deltaX, deltaY, distanceSquared, ai.movementSpeed);
                }
                break;
            case ATTACK:
                ai.stateTimeRemaining -= deltaTime;
                if (ai.stateTimeRemaining <= 0f) {
                    ai.attackPending = true;
                    enterTimedState(ai, EnemyAIComponent.State.RECOVER, ai.recoveryDuration);
                }
                break;
            case RECOVER:
                ai.stateTimeRemaining -= deltaTime;
                if (ai.stateTimeRemaining <= 0f) {
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
            case STUNNED:
                ai.stateTimeRemaining -= deltaTime;
                if (ai.stateTimeRemaining <= 0f) {
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
            case DEAD:
                break;
        }
    }

    private void moveTowardPlayer(
        VelocityComponent velocity,
        float deltaX,
        float deltaY,
        float distanceSquared,
        float speed
    ) {
        if (distanceSquared == 0f) {
            return;
        }

        float inverseDistance = 1f / (float) Math.sqrt(distanceSquared);
        velocity.vx = deltaX * inverseDistance * speed;
        velocity.vy = deltaY * inverseDistance * speed;
    }

    private void enterTimedState(
        EnemyAIComponent ai,
        EnemyAIComponent.State state,
        float duration
    ) {
        ai.state = state;
        ai.stateTimeRemaining = duration;
    }
}
