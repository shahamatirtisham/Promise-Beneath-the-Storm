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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.badlogic.gdx.Gdx;

/** Applies the player's active melee hit area to enemy health once per swing. */
public class DamageSystem extends IteratingSystem {
    /** Shared barrel/bomb radial damage. Barrel behavior retains its original immunity policy. */
    public static void applyRadialEnemyDamage(Iterable<Entity> enemies,
        PositionComponent center, float radius, float damage, boolean respectInvulnerability) {
        for (Entity enemy : enemies) {
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            com.github.shahamatirtisham.promise_beneath_the_storm.components.SkeletonComponent skeleton =
                enemy.getComponent(com.github.shahamatirtisham.promise_beneath_the_storm.components.SkeletonComponent.class);
            HealthComponent health = enemy.getComponent(HealthComponent.class);
            PositionComponent p = enemy.getComponent(PositionComponent.class);
            TeamComponent team =
                enemy.getComponent(TeamComponent.class);
            InvulnerabilityComponent immunity = enemy.getComponent(InvulnerabilityComponent.class);
            if (health == null || p == null || health.current <= 0f
                || (skeleton != null && skeleton.summoning)
                || (ai != null && ai.state == EnemyAIComponent.State.DEAD)
                || (team != null && team.team != TeamComponent.Team.ENEMY)
                || (respectInvulnerability && immunity != null && immunity.isActive())) continue;
            float dx = p.x - center.x, dy = p.y - center.y;
            if (dx * dx + dy * dy > radius * radius) continue;
            if (skeleton != null && skeleton.playerAttackBlocksRemaining > 0) {
                skeleton.playerAttackBlocksRemaining--;
                skeleton.blockVisualRequested = true;
                if (immunity != null) immunity.timeRemaining = immunity.duration;
                continue;
            }
            health.current = Math.max(0f, health.current - damage);
            if (respectInvulnerability) {
                if (immunity != null) immunity.timeRemaining = immunity.duration;
                if (ai != null && ai.state == EnemyAIComponent.State.IDLE && health.current > 0f)
                    ai.state = EnemyAIComponent.State.CHASE;
            }
        }
    }
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
        com.github.shahamatirtisham.promise_beneath_the_storm.components.SkeletonComponent skeleton =
            enemy.getComponent(com.github.shahamatirtisham.promise_beneath_the_storm.components.SkeletonComponent.class);
        if (skeleton != null && skeleton.summoning) {
            return;
        }
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
                : skeleton != null
                    ? com.github.shahamatirtisham.promise_beneath_the_storm.components.SkeletonComponent.BODY_RADIUS
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
        boolean skeletonBlocked = skeleton != null && skeleton.playerAttackBlocksRemaining > 0;
        if (skeletonBlocked) {
            skeleton.playerAttackBlocksRemaining--;
            skeleton.blockVisualRequested = true;
            damage = 0f;
            Gdx.app.log("Combat", "Skeleton blocked player attack ("
                + skeleton.playerAttackBlocksRemaining + " blocks remaining)");
        }
        ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
        if (shield != null
            && !shield.isGuardBroken()
            && isPlayerInFront(shield, enemyPosition, playerPosition)) {
            if (attack.knockbackStrength > 0f) {
                shield.guardBrokenTimeRemaining = shield.comboBreakDuration;
                shield.requestHitVisual();
                Gdx.app.log("Combat", "Combo finisher broke enemy guard");
            } else {
                shield.requestBlockVisual();
                Gdx.app.log("Combat", "Shield fully blocked melee damage");
            }
            // The intact shield absorbs even the impact that breaks it.
            damage = 0f;
        }
        health.current = Math.max(0f, health.current - damage);
        invulnerability.timeRemaining = invulnerability.duration;
        enemyData.lastPlayerAttackId = attack.attackId;

        if (!skeletonBlocked && attack.knockbackStrength > 0f && health.current > 0f) {
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
