package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;

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

        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        boolean facingAttacker = isFacingAttacker(playerPosition, enemyPosition);

        if (facingAttacker && defense.isParryActive()) {
            ai.state = EnemyAIComponent.State.STUNNED;
            ai.stateTimeRemaining = 0.8f;
            defense.feedbackTimeRemaining = 0.25f;
            Gdx.app.log("Combat", "Perfect parry - enemy stunned");
            return;
        }

        float damage = ai.attackDamage;
        if (facingAttacker && defense.blocking) {
            damage *= 1f - defense.damageReduction;
            Gdx.app.log("Combat", "Blocked damage: " + (int) damage);
        }

        playerHealth.current = Math.max(0f, playerHealth.current - damage);
        playerInvulnerability.timeRemaining = playerInvulnerability.duration;
    }

    private boolean isFacingAttacker(
        PositionComponent playerPosition,
        PositionComponent enemyPosition
    ) {
        float deltaX = enemyPosition.x - playerPosition.x;
        float deltaY = enemyPosition.y - playerPosition.y;
        float lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0f) {
            return true;
        }

        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        float dot = facing.x * deltaX * inverseLength
            + facing.y * deltaY * inverseLength;
        return dot >= 0.2f;
    }
}
