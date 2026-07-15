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

/** Applies the player's active melee hit area to enemy health once per swing. */
public class DamageSystem extends IteratingSystem {
    private static final float ATTACK_START_DISTANCE = 0.35f;
    private static final float ENEMY_RADIUS = 0.45f;

    private final Entity player;

    public DamageSystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            HealthComponent.class,
            InvulnerabilityComponent.class
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

        if (!isInsideAttackArea(playerPosition, facing, attack, enemyPosition)) {
            return;
        }

        HealthComponent health = enemy.getComponent(HealthComponent.class);
        health.current = Math.max(0f, health.current - attack.damage);
        invulnerability.timeRemaining = invulnerability.duration;
        enemyData.lastPlayerAttackId = attack.attackId;
    }

    private boolean isInsideAttackArea(
        PositionComponent attacker,
        FacingComponent facing,
        AttackComponent attack,
        PositionComponent target
    ) {
        float deltaX = target.x - attacker.x;
        float deltaY = target.y - attacker.y;

        float forward = deltaX * facing.x + deltaY * facing.y;
        if (forward < ATTACK_START_DISTANCE - ENEMY_RADIUS
            || forward > attack.reach + ENEMY_RADIUS) {
            return false;
        }

        float sideways = Math.abs(deltaX * -facing.y + deltaY * facing.x);
        float progress = Math.max(0f, Math.min(
            1f,
            (forward - ATTACK_START_DISTANCE) / (attack.reach - ATTACK_START_DISTANCE)
        ));
        float allowedHalfWidth = attack.halfWidth * progress + ENEMY_RADIUS;
        return sideways <= allowedHalfWidth;
    }
}
