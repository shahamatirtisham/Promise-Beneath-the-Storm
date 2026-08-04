package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Runs Irhos's phase-specific attacks, beginning with Iron Fist ground slams. */
public class BossCombatSystem extends IteratingSystem {
    private static final float SLAM_TRIGGER_RANGE = 5f;
    private static final float BETWEEN_SLAMS = 1.35f;

    private final Entity player;

    public BossCombatSystem(Entity player) {
        super(Family.all(
            BossComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class,
            PositionComponent.class,
            VelocityComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity boss, float deltaTime) {
        BossComponent data = boss.getComponent(BossComponent.class);
        if (data.phase != BossComponent.Phase.IRON_FIST) {
            return;
        }

        EnemyAIComponent ai = boss.getComponent(EnemyAIComponent.class);
        VelocityComponent velocity = boss.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        ai.attackPending = false;

        if (boss.getComponent(HealthComponent.class).current <= 0f
            || player.getComponent(PlayerComponent.class).dead) {
            return;
        }
        if (data.isTransitioning()) {
            data.attackState = BossComponent.AttackState.PURSUIT;
            data.attackTimeRemaining = BETWEEN_SLAMS;
            return;
        }

        PositionComponent bossPosition = boss.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - bossPosition.x;
        float deltaY = playerPosition.y - bossPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        switch (data.attackState) {
            case PURSUIT:
                ai.state = EnemyAIComponent.State.CHASE;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f
                    && distanceSquared <= SLAM_TRIGGER_RANGE * SLAM_TRIGGER_RANGE) {
                    data.slamTargetX = playerPosition.x;
                    data.slamTargetY = playerPosition.y;
                    data.attackState = BossComponent.AttackState.SLAM_WINDUP;
                    data.attackTimeRemaining = data.slamWindup;
                    ai.state = EnemyAIComponent.State.ATTACK;
                    Gdx.app.log("Boss", "Iron Fist slam telegraphed");
                } else {
                    moveTowardPlayer(velocity, deltaX, deltaY, distanceSquared, ai.movementSpeed);
                }
                break;
            case SLAM_WINDUP:
                ai.state = EnemyAIComponent.State.ATTACK;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f) {
                    resolveSlam(data);
                    data.attackState = BossComponent.AttackState.SLAM_RECOVERY;
                    data.attackTimeRemaining = data.slamRecovery;
                    ai.state = EnemyAIComponent.State.RECOVER;
                }
                break;
            case SLAM_RECOVERY:
                ai.state = EnemyAIComponent.State.RECOVER;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f) {
                    data.attackState = BossComponent.AttackState.PURSUIT;
                    data.attackTimeRemaining = BETWEEN_SLAMS;
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
        }
    }

    private void resolveSlam(BossComponent data) {
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - data.slamTargetX;
        float deltaY = playerPosition.y - data.slamTargetY;
        if (deltaX * deltaX + deltaY * deltaY > data.slamRadius * data.slamRadius) {
            Gdx.app.log("Boss", "Iron Fist slam dodged");
            return;
        }

        HealthComponent health = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent invulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        if (health.current <= 0f || invulnerability.isActive()) {
            return;
        }

        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        float damage = defense.blocking
            ? data.slamDamage * (1f - defense.damageReduction)
            : data.slamDamage;
        health.current = Math.max(0f, health.current - damage);
        invulnerability.timeRemaining = invulnerability.duration;
        Gdx.app.log(
            "Boss",
            defense.blocking
                ? "Iron Fist slam blocked: " + Math.round(damage) + " damage"
                : "Iron Fist slam hit: " + Math.round(damage) + " damage"
        );
    }

    private void moveTowardPlayer(
        VelocityComponent velocity,
        float deltaX,
        float deltaY,
        float distanceSquared,
        float speed
    ) {
        if (distanceSquared == 0f) {
            return;
        }
        float inverseDistance = 1f / (float) Math.sqrt(distanceSquared);
        velocity.vx = deltaX * inverseDistance * speed;
        velocity.vy = deltaY * inverseDistance * speed;
    }
}
