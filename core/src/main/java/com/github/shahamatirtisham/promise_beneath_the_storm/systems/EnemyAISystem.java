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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.WitchComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AnimationComponent;

/** Runs the first enemy's finite-state machine: idle, chase, attack, and recovery. */
public class EnemyAISystem extends IteratingSystem {
    private final Entity player;

    public EnemyAISystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            VelocityComponent.class
        ).exclude(
            com.github.shahamatirtisham.promise_beneath_the_storm.components.HeavyEnemyComponent.class,
            RangedEnemyComponent.class,
            ChargerComponent.class,
            WitchComponent.class,
            BossComponent.class
        ).get());
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
                    ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
                    if (shield != null) {
                        shield.beginAttack(enemy.getComponent(AnimationComponent.class));
                        ai.attackPending = false;
                        enterTimedState(ai, EnemyAIComponent.State.ATTACK, shield.attackAnimationDuration);
                    } else {
                        enterTimedState(ai, EnemyAIComponent.State.ATTACK, ai.attackWindup);
                    }
                } else {
                    moveTowardPlayer(velocity, deltaX, deltaY, distanceSquared, ai.movementSpeed);
                }
                break;
            case ATTACK:
                if (enemy.getComponent(ShieldGuardComponent.class) != null) {
                    updateShieldAttack(enemy, ai, deltaTime);
                    break;
                }
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

    private void updateShieldAttack(Entity enemy, EnemyAIComponent ai, float deltaTime) {
        ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
        AnimationComponent animation = enemy.getComponent(AnimationComponent.class);
        // Reactions remain visible and cannot consume the remainder of the attack.
        if (animation.hurtTimeRemaining > 0f || shield.blockVisualTimeRemaining > 0f
            || shield.hitVisualRequested || shield.blockVisualRequested) return;
        float nextTime = shield.attackAnimationTime + deltaTime;
        int currentFrame = animation.attack.getKeyFrameIndex(shield.attackAnimationTime);
        int nextFrame = animation.attack.getKeyFrameIndex(nextTime);
        if (nextFrame > currentFrame + 1) {
            // Even a long update must not skip unreadable attack frames.
            nextTime = Math.nextUp((currentFrame + 1) * animation.attack.getFrameDuration());
        }
        shield.attackAnimationTime = nextTime;
        ai.stateTimeRemaining = Math.max(0f, shield.attackAnimationDuration - nextTime);
        if (!shield.attackDamageQueued
            && nextTime >= shield.attackAnimationDuration * ShieldGuardComponent.ATTACK_HIT_PROGRESS) {
            shield.attackDamageQueued = true;
            ai.attackPending = true;
        }
        if (animation.attack.isAnimationFinished(nextTime) && shield.attackLastFrameShown) {
            enterTimedState(ai, EnemyAIComponent.State.RECOVER, ai.recoveryDuration);
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
