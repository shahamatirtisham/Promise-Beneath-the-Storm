package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ProjectileComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import java.util.function.IntSupplier;

/** Moves enemy projectiles and resolves their contact with the player. */
public class ProjectileSystem extends EntitySystem {
    private static final float PLAYER_RADIUS = 0.4f;

    private final Engine engine;
    private final Entity player;
    private final Array<Entity> projectiles;
    private final IntSupplier levelSupplier;

    public ProjectileSystem(
        Engine engine,
        Entity player,
        Array<Entity> projectiles,
        IntSupplier levelSupplier
    ) {
        this.engine = engine;
        this.player = player;
        this.projectiles = projectiles;
        this.levelSupplier = levelSupplier;
    }

    @Override
    public void update(float deltaTime) {
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent invulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        DefenseComponent defense = player.getComponent(DefenseComponent.class);

        for (int index = projectiles.size - 1; index >= 0; index--) {
            Entity projectile = projectiles.get(index);
            PositionComponent position = projectile.getComponent(PositionComponent.class);
            VelocityComponent velocity = projectile.getComponent(VelocityComponent.class);
            ProjectileComponent data = projectile.getComponent(ProjectileComponent.class);

            position.x += velocity.vx * deltaTime;
            position.y += velocity.vy * deltaTime;
            data.lifetimeRemaining -= deltaTime;

            float deltaX = playerPosition.x - position.x;
            float deltaY = playerPosition.y - position.y;
            float hitDistance = PLAYER_RADIUS + data.radius;
            boolean touchesPlayer = deltaX * deltaX + deltaY * deltaY
                <= hitDistance * hitDistance;

            if (touchesPlayer) {
                if (!playerState.dead && !invulnerability.isActive()) {
                    boolean facingProjectile = isFacingProjectile(
                        playerPosition,
                        position
                    );
                    if (facingProjectile && defense.isParryActive()) {
                        defense.feedbackTimeRemaining = 0.25f;
                        Gdx.app.log("Combat", "Perfect parry - projectile destroyed");
                    } else {
                        float damage = data.damage;
                        if (facingProjectile && defense.blocking) {
                            damage *= 1f - defense.damageReduction;
                            Gdx.app.log(
                                "Combat",
                                "Blocked projectile damage: " + (int) damage
                            );
                        }
                        playerHealth.current = Math.max(0f, playerHealth.current - damage);
                        invulnerability.timeRemaining = invulnerability.duration;
                        if (!facingProjectile || !defense.blocking) {
                            StatusEffectApplicator.applyForLevel(
                                player,
                                levelSupplier.getAsInt()
                            );
                        }
                    }
                }
                removeProjectile(index, projectile);
            } else if (data.lifetimeRemaining <= 0f) {
                removeProjectile(index, projectile);
            }
        }
    }

    private void removeProjectile(int index, Entity projectile) {
        projectiles.removeIndex(index);
        engine.removeEntity(projectile);
    }

    private boolean isFacingProjectile(
        PositionComponent playerPosition,
        PositionComponent projectilePosition
    ) {
        float deltaX = projectilePosition.x - playerPosition.x;
        float deltaY = projectilePosition.y - playerPosition.y;
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
