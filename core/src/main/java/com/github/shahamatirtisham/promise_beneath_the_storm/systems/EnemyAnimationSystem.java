package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.AnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.NecromancerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.WitchComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;

public class EnemyAnimationSystem extends IteratingSystem {

    public EnemyAnimationSystem() {

        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            AnimationComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {

        EnemyAIComponent ai =
            enemy.getComponent(EnemyAIComponent.class);

        AnimationComponent animation =
            enemy.getComponent(AnimationComponent.class);

        InvulnerabilityComponent invulnerability =
            enemy.getComponent(InvulnerabilityComponent.class);

        ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
        if (shield != null) {
            updateShieldGuard(enemy, shield, ai, animation, deltaTime);
            return;
        }

        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        if (witch != null) {
            updateWitch(enemy, witch, ai, animation, deltaTime);
            return;
        }

        if (ai.state != EnemyAIComponent.State.DEAD
            && enemy.getComponent(NecromancerComponent.class) != null
            && invulnerability != null
            && invulnerability.isActive()) {
            setAnimationState(animation, AnimationComponent.State.HURT);
            animation.stateTime += deltaTime;
            return;
        }

        /*
         * Convert the existing gameplay AI state
         * into the corresponding visual animation.
         */
        switch (ai.state) {

            case IDLE:
                setAnimationState(
                    animation,
                    AnimationComponent.State.IDLE
                );
                break;

            case CHASE:
                setAnimationState(
                    animation,
                    AnimationComponent.State.WALK
                );
                break;

            case ATTACK:
                setAnimationState(
                    animation,
                    AnimationComponent.State.ATTACK
                );
                break;

            case RECOVER:
                setAnimationState(
                    animation,
                    AnimationComponent.State.IDLE
                );
                break;

            case STUNNED:
                setAnimationState(
                    animation,
                    AnimationComponent.State.HURT
                );
                break;

            case DEAD:
                setAnimationState(
                    animation,
                    AnimationComponent.State.DEAD
                );
                break;
        }

        /*
         * Advance the current animation.
         */
        animation.stateTime += deltaTime;
    }

    private void updateShieldGuard(Entity enemy, ShieldGuardComponent shield,
        EnemyAIComponent ai, AnimationComponent animation, float deltaTime) {
        float health = enemy.getComponent(HealthComponent.class).current;
        boolean damaged = health < animation.previousHealth;
        animation.previousHealth = health;
        if (shield.facingX != 0f) animation.facingLeft = shield.facingX < 0f;
        boolean newAIState = ai.state != shield.previousAnimationAIState;
        shield.previousAnimationAIState = ai.state;
        if (health <= 0f || ai.state == EnemyAIComponent.State.DEAD) {
            shield.blockVisualRequested = false;
            shield.hitVisualRequested = false;
            shield.blockVisualTimeRemaining = 0f;
            animation.hurtTimeRemaining = 0f;
            if (animation.state != AnimationComponent.State.DEAD) {
                setAnimationState(animation, AnimationComponent.State.DEAD);
            } else animation.stateTime += deltaTime;
            return;
        }
        if (damaged || shield.hitVisualRequested
            || (newAIState && ai.state == EnemyAIComponent.State.STUNNED)) {
            shield.hitVisualRequested = false;
            shield.blockVisualRequested = false;
            shield.blockVisualTimeRemaining = 0f;
            animation.hurtTimeRemaining = animation.hurt.getAnimationDuration();
            setAnimationState(animation, AnimationComponent.State.HURT);
            animation.stateTime = 0f;
            return;
        }
        if (animation.hurtTimeRemaining > 0f) {
            shield.blockVisualRequested = false;
            animation.stateTime += deltaTime;
            animation.hurtTimeRemaining = Math.max(0f, animation.hurtTimeRemaining - deltaTime);
            return;
        }
        if (shield.isGuardBroken()) {
            shield.blockVisualRequested = false;
            shield.blockVisualTimeRemaining = 0f;
        }
        if (shield.blockVisualRequested) {
            shield.blockVisualRequested = false;
            shield.blockVisualTimeRemaining = animation.block.getAnimationDuration();
            setAnimationState(animation, AnimationComponent.State.BLOCK);
            animation.stateTime = 0f;
            return;
        }
        if (shield.blockVisualTimeRemaining > 0f) {
            animation.stateTime += deltaTime;
            shield.blockVisualTimeRemaining = Math.max(0f, shield.blockVisualTimeRemaining - deltaTime);
            return;
        }
        switch (ai.state) {
            case ATTACK:
                setAnimationState(animation, AnimationComponent.State.ATTACK);
                animation.stateTime = shield.attackAnimationTime;
                if (animation.attack.getKeyFrameIndex(animation.stateTime)
                    == animation.attack.getKeyFrames().length - 1) shield.attackLastFrameShown = true;
                break;
            case CHASE:
                setAnimationState(animation, AnimationComponent.State.WALK);
                animation.stateTime += deltaTime;
                break;
            case STUNNED:
                setAnimationState(animation, AnimationComponent.State.HURT);
                animation.stateTime += deltaTime; // NORMAL holds the final hit frame.
                break;
            default:
                setAnimationState(animation, AnimationComponent.State.IDLE);
                animation.stateTime += deltaTime;
                break;
        }
    }

    /** Visual-only mapping; never writes to the witch's gameplay state or direction. */
    private void updateWitch(Entity enemy, WitchComponent witch, EnemyAIComponent ai,
        AnimationComponent animation, float deltaTime) {
        float health = enemy.getComponent(HealthComponent.class).current;
        boolean damaged = health < animation.previousHealth;
        animation.previousHealth = health;
        animation.facingLeft = witch.facingLeft;
        if (health <= 0f || ai.state == EnemyAIComponent.State.DEAD) {
            animation.hurtTimeRemaining = 0f;
            setAnimationState(animation, AnimationComponent.State.DEAD);
            animation.stateTime += deltaTime;
            return;
        }
        if (damaged) {
            animation.hurtTimeRemaining = animation.hurt.getAnimationDuration();
            animation.stateTime = 0f;
            setAnimationState(animation, AnimationComponent.State.HURT);
        }
        if (animation.hurtTimeRemaining > 0f) {
            animation.stateTime += deltaTime;
            animation.hurtTimeRemaining = Math.max(0f, animation.hurtTimeRemaining - deltaTime);
            return;
        }

        switch (witch.state) {
            case CHARGE_WINDUP:
            case CHARGING:
                setAnimationState(animation, AnimationComponent.State.CHARGE);
                float chargeDuration = animation.charge.getAnimationDuration();
                animation.stateTime = witch.stateTime % chargeDuration;
                break;
            case SIDE_ATTACK_WINDUP:
            case SIDE_ATTACK:
                setAnimationState(animation, AnimationComponent.State.ATTACK);
                animation.stateTime = witch.getSideAttackAnimationTime();
                break;
            case IDLE:
            case CHARGE_RECOVERY:
            case SIDE_ATTACK_RECOVERY:
            case STUNNED:
                setAnimationState(animation, AnimationComponent.State.IDLE);
                animation.stateTime += deltaTime;
                break;
            default:
                // Use resolved movement so a witch stopped by a wall can idle.
                boolean moving = enemy.getComponent(PhysicsComponent.class)
                    .body.getLinearVelocity().len2() > 0.0001f;
                setAnimationState(animation, moving ? AnimationComponent.State.WALK
                    : AnimationComponent.State.IDLE);
                animation.stateTime += deltaTime;
                break;
        }
    }

    private void setAnimationState(
        AnimationComponent animation,
        AnimationComponent.State newState
    ) {

        /*
         * Don't reset the animation every frame.
         */
        if (animation.state != newState) {

            animation.state = newState;

            /*
             * Start the new animation from frame 0.
             */
            animation.stateTime = 0f;
        }
    }
}
