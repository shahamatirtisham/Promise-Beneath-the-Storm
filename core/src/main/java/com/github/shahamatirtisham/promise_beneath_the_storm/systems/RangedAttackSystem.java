package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RangedEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.ProjectileFactory;

/** Makes ranged prototypes periodically fire directly at the player. */
public class RangedAttackSystem extends IteratingSystem {
    private final Engine engine;
    private final Entity player;
    private final Array<Entity> projectiles;

    public RangedAttackSystem(Engine engine, Entity player, Array<Entity> projectiles) {
        super(Family.all(
            RangedEnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            HealthComponent.class
        ).get());
        this.engine = engine;
        this.player = player;
        this.projectiles = projectiles;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        RangedEnemyComponent ranged = enemy.getComponent(RangedEnemyComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (ai.state == EnemyAIComponent.State.DEAD
            || ai.state == EnemyAIComponent.State.STUNNED
            || playerState.dead
            || playerState.controlsLocked) {
            return;
        }

        ranged.cooldownRemaining -= deltaTime;
        if (ranged.cooldownRemaining > 0f) {
            return;
        }

        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - enemyPosition.x;
        float deltaY = playerPosition.y - enemyPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;
        if (distanceSquared == 0f
            || distanceSquared > ranged.attackRange * ranged.attackRange) {
            return;
        }

        float inverseDistance = 1f / (float) Math.sqrt(distanceSquared);
        Entity projectile = ProjectileFactory.createEnemyProjectile(
            enemyPosition.x,
            enemyPosition.y,
            deltaX * inverseDistance,
            deltaY * inverseDistance,
            ranged.projectileSpeed,
            ai.attackDamage
        );
        projectiles.add(projectile);
        engine.addEntity(projectile);
        ranged.cooldownRemaining = ranged.attackCooldown;
    }
}
