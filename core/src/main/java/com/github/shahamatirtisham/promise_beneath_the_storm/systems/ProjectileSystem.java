package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HeavyEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BreakablePotComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.BreakablePotFactory;
import java.util.function.IntSupplier;

/** Moves enemy projectiles and resolves their contact with the player. */
public class ProjectileSystem extends EntitySystem {
    private static final float PLAYER_RADIUS = 0.4f;

    private final Engine engine;
    private final Entity player;
    private final Array<Entity> projectiles;
    private final Array<Entity> enemies;
    private final IntSupplier levelSupplier;
    private final BreakablePotSystem breakablePotSystem;

    public ProjectileSystem(
        Engine engine,
        Entity player,
        Array<Entity> projectiles,
        Array<Entity> enemies,
        IntSupplier levelSupplier,
        BreakablePotSystem breakablePotSystem
    ) {
        this.engine = engine;
        this.player = player;
        this.projectiles = projectiles;
        this.enemies = enemies;
        this.levelSupplier = levelSupplier;
        this.breakablePotSystem = breakablePotSystem;
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

            TeamComponent team = projectile.getComponent(TeamComponent.class);
            if (team.team == TeamComponent.Team.PLAYER) {
                if (hitPot(position, data) || hitEnemy(position, data)) {
                    removeProjectile(index, projectile);
                } else if (data.lifetimeRemaining <= 0f) {
                    removeProjectile(index, projectile);
                }
                continue;
            }

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
                        requestParryAnimation();
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
                            int statusLevel = data.statusLevelOverride > 0
                                ? data.statusLevelOverride
                                : levelSupplier.getAsInt();
                            StatusEffectApplicator.applyForLevel(
                                player,
                                statusLevel
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

    private boolean hitPot(PositionComponent projectilePosition, ProjectileComponent data) {
        com.badlogic.ashley.utils.ImmutableArray<Entity> pots =
            engine.getEntitiesFor(Family.all(
                BreakablePotComponent.class,
                PositionComponent.class
            ).get());
        for (int index = 0; index < pots.size(); index++) {
            Entity potEntity = pots.get(index);
            BreakablePotComponent pot =
                potEntity.getComponent(BreakablePotComponent.class);
            if (pot.state != BreakablePotComponent.State.IDLE) {
                continue;
            }
            PositionComponent potPosition =
                potEntity.getComponent(PositionComponent.class);
            float deltaX = potPosition.x - projectilePosition.x;
            float deltaY = potPosition.y - projectilePosition.y;
            float hitDistance = BreakablePotFactory.HIT_RADIUS + data.radius;
            if (deltaX * deltaX + deltaY * deltaY <= hitDistance * hitDistance) {
                return breakablePotSystem.hit(potEntity);
            }
        }
        return false;
    }

    private void requestParryAnimation() {
        PlayerAnimationComponent animation =
            player.getComponent(PlayerAnimationComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        if (animation != null && facing != null) {
            animation.requestParryAnimation(facing.x, facing.y);
        }
    }

    private boolean hitEnemy(PositionComponent projectilePosition, ProjectileComponent data) {
        for (int index = 0; index < enemies.size; index++) {
            Entity enemy = enemies.get(index);
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (ai == null || ai.state == EnemyAIComponent.State.DEAD) {
                continue;
            }
            InvulnerabilityComponent invulnerability =
                enemy.getComponent(InvulnerabilityComponent.class);
            if (invulnerability.isActive()) {
                continue;
            }

            PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
            float radius = enemy.getComponent(BossComponent.class) != null ? 0.8f
                : enemy.getComponent(HeavyEnemyComponent.class) != null ? 0.65f : 0.45f;
            float deltaX = enemyPosition.x - projectilePosition.x;
            float deltaY = enemyPosition.y - projectilePosition.y;
            float hitDistance = radius + data.radius;
            if (deltaX * deltaX + deltaY * deltaY > hitDistance * hitDistance) {
                continue;
            }

            float damage = data.damage;
            ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
            if (shield != null && !shield.isGuardBroken()
                && shieldFacesProjectile(shield, enemyPosition, projectilePosition)) {
                damage *= 1f - shield.frontalDamageReduction;
                Gdx.app.log("Combat", "Shield blocked most knife damage");
            }
            HealthComponent health = enemy.getComponent(HealthComponent.class);
            health.current = Math.max(0f, health.current - damage);
            invulnerability.timeRemaining = invulnerability.duration;
            if (health.current > 0f && ai.state == EnemyAIComponent.State.IDLE) {
                ai.state = EnemyAIComponent.State.CHASE;
                Gdx.app.log("Combat", "Enemy alerted by ranged attack");
            }
            return true;
        }
        return false;
    }

    private boolean shieldFacesProjectile(
        ShieldGuardComponent shield,
        PositionComponent guard,
        PositionComponent projectile
    ) {
        float deltaX = projectile.x - guard.x;
        float deltaY = projectile.y - guard.y;
        float lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0f) {
            return true;
        }
        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        return shield.facingX * deltaX * inverseLength
            + shield.facingY * deltaY * inverseLength >= 0.2f;
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
