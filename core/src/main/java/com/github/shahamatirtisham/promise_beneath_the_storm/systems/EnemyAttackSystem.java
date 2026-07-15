package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Resolves a telegraphed enemy strike once its wind-up completes. */
public class EnemyAttackSystem extends IteratingSystem {
    private static final float PLAYER_RADIUS = 0.4f;

    private final Entity player;

    public EnemyAttackSystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            HealthComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        if (!ai.attackPending) {
            return;
        }
        ai.attackPending = false;

        if (enemy.getComponent(HealthComponent.class).current <= 0f) {
            return;
        }

        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent playerInvulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        if (playerHealth.current <= 0f || playerInvulnerability.isActive()) {
            return;
        }

        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - enemyPosition.x;
        float deltaY = playerPosition.y - enemyPosition.y;
        float hitRange = ai.attackRange + PLAYER_RADIUS;

        if (deltaX * deltaX + deltaY * deltaY > hitRange * hitRange) {
            return;
        }

        playerHealth.current = Math.max(0f, playerHealth.current - ai.attackDamage);
        playerInvulnerability.timeRemaining = playerInvulnerability.duration;
    }
}
