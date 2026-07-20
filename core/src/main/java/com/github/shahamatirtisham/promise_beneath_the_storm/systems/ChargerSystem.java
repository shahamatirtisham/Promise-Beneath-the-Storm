package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChargerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Runs the telegraph, committed rush, impact, and vulnerability cycle. */
public class ChargerSystem extends IteratingSystem {
    private static final float CONTACT_RANGE_SQUARED = 0.9f * 0.9f;
    private final Entity player;

    public ChargerSystem(Entity player) {
        super(Family.all(
            ChargerComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            VelocityComponent.class,
            HealthComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        ChargerComponent charger = enemy.getComponent(ChargerComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (ai.state == EnemyAIComponent.State.DEAD
            || playerState.dead || playerState.controlsLocked) {
            return;
        }
        if (ai.state == EnemyAIComponent.State.STUNNED
            && charger.state != ChargerComponent.State.STUNNED) {
            charger.state = ChargerComponent.State.STUNNED;
            charger.stateTimeRemaining = Math.max(
                charger.stunDuration,
                ai.stateTimeRemaining
            );
        }

        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - enemyPosition.x;
        float deltaY = playerPosition.y - enemyPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        switch (charger.state) {
            case APPROACH:
                ai.state = EnemyAIComponent.State.CHASE;
                if (distanceSquared <= charger.triggerRange * charger.triggerRange) {
                    lockChargeDirection(charger, deltaX, deltaY, distanceSquared);
                    charger.state = ChargerComponent.State.WINDUP;
                    charger.stateTimeRemaining = charger.windupDuration;
                    ai.state = EnemyAIComponent.State.ATTACK;
                } else {
                    moveNormalized(velocity, deltaX, deltaY, distanceSquared, ai.movementSpeed);
                }
                break;
            case WINDUP:
                ai.state = EnemyAIComponent.State.ATTACK;
                charger.stateTimeRemaining -= deltaTime;
                if (charger.stateTimeRemaining <= 0f) {
                    charger.state = ChargerComponent.State.CHARGING;
                    charger.stateTimeRemaining = charger.chargeDuration;
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
            case CHARGING:
                ai.state = EnemyAIComponent.State.CHASE;
                velocity.vx = charger.directionX * charger.chargeSpeed;
                velocity.vy = charger.directionY * charger.chargeSpeed;
                charger.stateTimeRemaining -= deltaTime;
                if (distanceSquared <= CONTACT_RANGE_SQUARED) {
                    resolvePlayerImpact(enemyPosition, playerPosition, ai);
                    enterStun(charger, ai);
                } else if (charger.stateTimeRemaining <= 0f) {
                    enterStun(charger, ai);
                }
                break;
            case STUNNED:
                ai.state = EnemyAIComponent.State.STUNNED;
                charger.stateTimeRemaining -= deltaTime;
                if (charger.stateTimeRemaining <= 0f) {
                    charger.state = ChargerComponent.State.APPROACH;
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
        }
    }

    private void resolvePlayerImpact(
        PositionComponent enemyPosition,
        PositionComponent playerPosition,
        EnemyAIComponent ai
    ) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent invulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        if (health.current <= 0f || invulnerability.isActive()) {
            return;
        }

        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        boolean facing = isFacing(playerPosition, enemyPosition);
        if (facing && defense.isParryActive()) {
            defense.feedbackTimeRemaining = 0.25f;
            Gdx.app.log("Combat", "Perfect parry - charge interrupted");
            return;
        }

        float damage = ai.attackDamage * 1.35f;
        if (facing && defense.blocking) {
            damage *= 1f - defense.damageReduction;
        }
        health.current = Math.max(0f, health.current - damage);
        invulnerability.timeRemaining = invulnerability.duration;
    }

    private boolean isFacing(PositionComponent playerPosition, PositionComponent enemyPosition) {
        float deltaX = enemyPosition.x - playerPosition.x;
        float deltaY = enemyPosition.y - playerPosition.y;
        float lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0f) {
            return true;
        }
        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        return facing.x * deltaX * inverseLength + facing.y * deltaY * inverseLength >= 0.2f;
    }

    private void lockChargeDirection(
        ChargerComponent charger,
        float deltaX,
        float deltaY,
        float distanceSquared
    ) {
        if (distanceSquared == 0f) {
            charger.directionX = 1f;
            charger.directionY = 0f;
            return;
        }
        float inverseDistance = 1f / (float) Math.sqrt(distanceSquared);
        charger.directionX = deltaX * inverseDistance;
        charger.directionY = deltaY * inverseDistance;
    }

    private void moveNormalized(
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

    private void enterStun(ChargerComponent charger, EnemyAIComponent ai) {
        charger.state = ChargerComponent.State.STUNNED;
        charger.stateTimeRemaining = charger.stunDuration;
        ai.state = EnemyAIComponent.State.STUNNED;
    }
}
