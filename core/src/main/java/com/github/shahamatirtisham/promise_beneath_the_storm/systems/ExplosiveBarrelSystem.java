package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ExplosiveBarrelComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Handles melee damage, explosions, enemy damage, player damage, and chain reactions. */
public class ExplosiveBarrelSystem extends EntitySystem {
    private static final float BARREL_RADIUS = 0.4f;
    private final Entity player;
    private final Array<Entity> enemies;
    private final Array<Entity> barrels;

    public ExplosiveBarrelSystem(
        Entity player,
        Array<Entity> enemies,
        Array<Entity> barrels
    ) {
        this.player = player;
        this.enemies = enemies;
        this.barrels = barrels;
    }

    @Override
    public void update(float deltaTime) {
        for (int barrelIndex = 0; barrelIndex < barrels.size; barrelIndex++) {
            Entity barrel = barrels.get(barrelIndex);
            ExplosiveBarrelComponent data =
                barrel.getComponent(ExplosiveBarrelComponent.class);
            if (data.destroyed) {
                if (!data.explosionApplied) {
                    data.fuseTimeRemaining -= deltaTime;
                    if (data.fuseTimeRemaining <= 0f) {
                        data.explosionTimeRemaining = 0.3f;
                        applyExplosion(barrel, data);
                    }
                } else {
                    data.explosionTimeRemaining = Math.max(
                        0f,
                        data.explosionTimeRemaining - deltaTime
                    );
                }
                continue;
            }
            applyPlayerAttack(barrel, data);
            if (data.health <= 0f) {
                data.destroyed = true;
                data.fuseTimeRemaining = 0.45f;
            }
        }
    }

    private void applyPlayerAttack(Entity barrel, ExplosiveBarrelComponent data) {
        AttackComponent attack = player.getComponent(AttackComponent.class);
        if (!attack.isActive() || data.lastPlayerAttackId == attack.attackId) {
            return;
        }
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent barrelPosition = barrel.getComponent(PositionComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        float deltaX = barrelPosition.x - playerPosition.x;
        float deltaY = barrelPosition.y - playerPosition.y;
        float forward = deltaX * facing.x + deltaY * facing.y;
        float sideways = Math.abs(deltaX * -facing.y + deltaY * facing.x);
        if (forward < -BARREL_RADIUS
            || forward > attack.reach + BARREL_RADIUS
            || sideways > attack.halfWidth + BARREL_RADIUS) {
            return;
        }
        data.health -= attack.damage;
        data.lastPlayerAttackId = attack.attackId;
    }

    private void applyExplosion(Entity source, ExplosiveBarrelComponent data) {
        if (data.explosionApplied) {
            return;
        }
        data.explosionApplied = true;
        PositionComponent sourcePosition = source.getComponent(PositionComponent.class);
        float radiusSquared = data.explosionRadius * data.explosionRadius;

        for (Entity enemy : enemies) {
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (ai.state == EnemyAIComponent.State.DEAD) {
                continue;
            }
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            if (distanceSquared(sourcePosition, position) <= radiusSquared) {
                HealthComponent health = enemy.getComponent(HealthComponent.class);
                health.current = Math.max(0f, health.current - data.explosionDamage);
            }
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        InvulnerabilityComponent playerInvulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        if (distanceSquared(sourcePosition, playerPosition) <= radiusSquared
            && !playerInvulnerability.isActive()) {
            HealthComponent playerHealth = player.getComponent(HealthComponent.class);
            playerHealth.current = Math.max(
                0f,
                playerHealth.current - data.explosionDamage
            );
            playerInvulnerability.timeRemaining = playerInvulnerability.duration;
        }

        for (int barrelIndex = 0; barrelIndex < barrels.size; barrelIndex++) {
            Entity barrel = barrels.get(barrelIndex);
            if (barrel == source) {
                continue;
            }
            PositionComponent position = barrel.getComponent(PositionComponent.class);
            if (distanceSquared(sourcePosition, position) <= radiusSquared) {
                barrel.getComponent(ExplosiveBarrelComponent.class).health = 0f;
            }
        }
    }

    private float distanceSquared(PositionComponent first, PositionComponent second) {
        float deltaX = first.x - second.x;
        float deltaY = first.y - second.y;
        return deltaX * deltaX + deltaY * deltaY;
    }
}
