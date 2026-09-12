package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.KnockbackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HeavyEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.badlogic.gdx.Gdx;

/** Applies the player's active melee hit area to enemy health once per swing. */
public class DamageSystem extends IteratingSystem {
    private static final float ENEMY_RADIUS = 0.45f;

    private final Entity player;

    public DamageSystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            HealthComponent.class,
            InvulnerabilityComponent.class,
            KnockbackComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        InvulnerabilityComponent invulnerability =
            enemy.getComponent(InvulnerabilityComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        if (ai.state == EnemyAIComponent.State.DEAD) {
            return;
        }

        AttackComponent attack = player.getComponent(AttackComponent.class);
        EnemyComponent enemyData = enemy.getComponent(EnemyComponent.class);
        if (!attack.isActive()
            || invulnerability.isActive()
            || enemyData.lastPlayerAttackId == attack.attackId) {
            return;
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);

        float enemyRadius = enemy.getComponent(BossComponent.class) != null
            ? 0.8f
            : enemy.getComponent(HeavyEnemyComponent.class) != null
                ? 0.65f
                : ENEMY_RADIUS;
        if (!AttackHitbox.overlaps(
            playerPosition,
            facing,
            attack,
            enemyPosition,
            enemyRadius
        )) {
            return;
        }

        HealthComponent health = enemy.getComponent(HealthComponent.class);
        float damage = attack.damage;
        ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
        if (shield != null
            && !shield.isGuardBroken()
            && isPlayerInFront(shield, enemyPosition, playerPosition)) {
            if (attack.knockbackStrength > 0f) {
                shield.guardBrokenTimeRemaining = shield.comboBreakDuration;
                Gdx.app.log("Combat", "Combo finisher broke enemy guard");
            } else {
                damage *= 1f - shield.frontalDamageReduction;
                Gdx.app.log("Combat", "Shield blocked most melee damage");
            }
        }
        health.current = Math.max(0f, health.current - damage);
        invulnerability.timeRemaining = invulnerability.duration;
        enemyData.lastPlayerAttackId = attack.attackId;

        if (attack.knockbackStrength > 0f && health.current > 0f) {
            KnockbackComponent knockback = enemy.getComponent(KnockbackComponent.class);
            knockback.timeRemaining = 0.16f;
            knockback.velocityX = facing.x * attack.knockbackStrength;
            knockback.velocityY = facing.y * attack.knockbackStrength;
            ai.state = EnemyAIComponent.State.STUNNED;
            ai.stateTimeRemaining = 0.2f;
        }
    }

    private boolean isPlayerInFront(
        ShieldGuardComponent shield,
        PositionComponent guard,
        PositionComponent playerPosition
    ) {
        float deltaX = playerPosition.x - guard.x;
        float deltaY = playerPosition.y - guard.y;
        float lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0f) {
            return true;
        }
        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        float dot = shield.facingX * deltaX * inverseLength
            + shield.facingY * deltaY * inverseLength;
        return dot >= 0.2f;
    }
}
