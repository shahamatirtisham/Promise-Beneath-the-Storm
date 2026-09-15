package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.WitchComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
import java.util.function.IntSupplier;

/** Resolves a telegraphed enemy strike once its wind-up completes. */
public class EnemyAttackSystem extends IteratingSystem {
    private static final float PLAYER_RADIUS = 0.4f;

    private final Entity player;
    private final IntSupplier levelSupplier;
    private final Rectangle witchAttackBounds = new Rectangle();

    public EnemyAttackSystem(Entity player, IntSupplier levelSupplier) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            PositionComponent.class,
            HealthComponent.class
        ).exclude(BossComponent.class).get());
        this.player = player;
        this.levelSupplier = levelSupplier;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        if (witch != null && witch.state != WitchComponent.State.CHARGING) {
            if (!witch.sideAttackDamagePending || witch.sideAttackDamageChecked) return;
            // Sample the player only at the cast's frame-7 opportunity. A miss or
            // invulnerable player cannot be hit later by walking into frame 8.
            witch.sideAttackDamageChecked = true;
        }
        try {
            resolveAttack(enemy);
        } finally {
            if (witch != null) witch.sideAttackDamagePending = false;
        }
    }

    private void resolveAttack(Entity enemy) {
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        if (witch == null && !ai.attackPending) {
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

        boolean overlaps = witch != null
            ? WitchSystem.overlapsPlayer(enemy, player, PLAYER_RADIUS, witchAttackBounds)
            : deltaX * deltaX + deltaY * deltaY <= hitRange * hitRange;
        if (!overlaps) {
            return;
        }

        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        boolean facingAttacker = isFacingAttacker(playerPosition, enemyPosition);

        if (facingAttacker && defense.isParryActive()) {
            if (witch != null) WitchSystem.markAttackResolved(witch);
            ai.state = EnemyAIComponent.State.STUNNED;
            ai.stateTimeRemaining = 1f;
            ShieldGuardComponent shield =
                enemy.getComponent(ShieldGuardComponent.class);
            if (shield != null) {
                shield.guardBrokenTimeRemaining = shield.parryBreakDuration;
                shield.requestHitVisual();
            }
            defense.feedbackTimeRemaining = 0.25f;
            requestParryAnimation();
            Gdx.app.log("Combat", "Perfect parry - enemy stunned");
            return;
        }

        float damage = ai.attackDamage;
        if (witch != null) {
            damage *= witch.state == WitchComponent.State.CHARGING
                ? witch.chargeDamageMultiplier : witch.sideDamageMultiplier;
        }
        if (facingAttacker && defense.blocking) {
            damage *= 1f - defense.damageReduction;
            Gdx.app.log("Combat", "Blocked damage: " + (int) damage);
        }

        playerHealth.current = Math.max(0f, playerHealth.current - damage);
        playerInvulnerability.timeRemaining = playerInvulnerability.duration;
        if (witch != null) {
            WitchSystem.markAttackResolved(witch);
            Gdx.app.log("Witch", witch.state == WitchComponent.State.CHARGING
                ? "Charge hit player" : "Side attack hit player");
        }
        if (!facingAttacker || !defense.blocking) {
            StatusEffectApplicator.applyForLevel(player, levelSupplier.getAsInt());
        }
    }

    private void requestParryAnimation() {
        PlayerAnimationComponent animation =
            player.getComponent(PlayerAnimationComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        if (animation != null && facing != null) {
            animation.requestParryAnimation(facing.x, facing.y);
        }
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
