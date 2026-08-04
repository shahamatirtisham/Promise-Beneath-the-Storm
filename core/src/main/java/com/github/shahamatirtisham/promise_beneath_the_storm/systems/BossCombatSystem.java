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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Runs Irhos's phase-specific attacks, beginning with Iron Fist ground slams. */
public class BossCombatSystem extends IteratingSystem {
    private static final float SLAM_TRIGGER_RANGE = 5f;
    private static final float BETWEEN_SLAMS = 1.35f;
    private static final float PUNCH_TRIGGER_RANGE = 6f;
    private static final float BETWEEN_PUNCHES = 0.9f;
    private static final float PUNCH_HIT_RANGE_SQUARED = 1.2f * 1.2f;

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
        if (data.phase != BossComponent.Phase.IRON_FIST
            && data.phase != BossComponent.Phase.BURNING_GAUNTLETS) {
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
            data.attackState = data.phase == BossComponent.Phase.IRON_FIST
                ? BossComponent.AttackState.PURSUIT
                : BossComponent.AttackState.BURNING_PURSUIT;
            data.attackTimeRemaining = data.phase == BossComponent.Phase.IRON_FIST
                ? BETWEEN_SLAMS
                : BETWEEN_PUNCHES;
            return;
        }

        PositionComponent bossPosition = boss.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - bossPosition.x;
        float deltaY = playerPosition.y - bossPosition.y;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        if (data.phase == BossComponent.Phase.BURNING_GAUNTLETS) {
            processBurningGauntlets(
                data,
                ai,
                velocity,
                bossPosition,
                playerPosition,
                deltaX,
                deltaY,
                distanceSquared,
                deltaTime
            );
            return;
        }

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

    private void processBurningGauntlets(
        BossComponent data,
        EnemyAIComponent ai,
        VelocityComponent velocity,
        PositionComponent bossPosition,
        PositionComponent playerPosition,
        float deltaX,
        float deltaY,
        float distanceSquared,
        float deltaTime
    ) {
        if (data.attackState != BossComponent.AttackState.BURNING_PURSUIT
            && data.attackState != BossComponent.AttackState.FLAME_PUNCH_WINDUP
            && data.attackState != BossComponent.AttackState.FLAME_PUNCH_DASH
            && data.attackState != BossComponent.AttackState.FLAME_PUNCH_RECOVERY) {
            data.attackState = BossComponent.AttackState.BURNING_PURSUIT;
            data.attackTimeRemaining = BETWEEN_PUNCHES;
        }

        switch (data.attackState) {
            case BURNING_PURSUIT:
                ai.state = EnemyAIComponent.State.CHASE;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f
                    && distanceSquared <= PUNCH_TRIGGER_RANGE * PUNCH_TRIGGER_RANGE) {
                    lockPunchDirection(data, bossPosition, deltaX, deltaY, distanceSquared);
                    data.attackState = BossComponent.AttackState.FLAME_PUNCH_WINDUP;
                    data.attackTimeRemaining = data.punchWindup;
                    ai.state = EnemyAIComponent.State.ATTACK;
                    Gdx.app.log("Boss", "Burning Gauntlets punch telegraphed");
                } else {
                    moveTowardPlayer(velocity, deltaX, deltaY, distanceSquared, ai.movementSpeed);
                }
                break;
            case FLAME_PUNCH_WINDUP:
                ai.state = EnemyAIComponent.State.ATTACK;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f) {
                    data.attackState = BossComponent.AttackState.FLAME_PUNCH_DASH;
                    data.attackTimeRemaining = data.punchDuration;
                    data.punchHit = false;
                }
                break;
            case FLAME_PUNCH_DASH:
                ai.state = EnemyAIComponent.State.ATTACK;
                velocity.vx = data.punchDirectionX * data.punchSpeed;
                velocity.vy = data.punchDirectionY * data.punchSpeed;
                resolvePunchContact(data, bossPosition, playerPosition);
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f) {
                    data.attackState = BossComponent.AttackState.FLAME_PUNCH_RECOVERY;
                    data.attackTimeRemaining = data.punchRecovery;
                    ai.state = EnemyAIComponent.State.RECOVER;
                }
                break;
            case FLAME_PUNCH_RECOVERY:
                ai.state = EnemyAIComponent.State.RECOVER;
                data.attackTimeRemaining -= deltaTime;
                if (data.attackTimeRemaining <= 0f) {
                    data.attackState = BossComponent.AttackState.BURNING_PURSUIT;
                    data.attackTimeRemaining = BETWEEN_PUNCHES;
                    ai.state = EnemyAIComponent.State.CHASE;
                }
                break;
            default:
                break;
        }
    }

    private void lockPunchDirection(
        BossComponent data,
        PositionComponent bossPosition,
        float deltaX,
        float deltaY,
        float distanceSquared
    ) {
        data.punchOriginX = bossPosition.x;
        data.punchOriginY = bossPosition.y;
        if (distanceSquared == 0f) {
            data.punchDirectionX = 1f;
            data.punchDirectionY = 0f;
            return;
        }
        float inverseDistance = 1f / (float) Math.sqrt(distanceSquared);
        data.punchDirectionX = deltaX * inverseDistance;
        data.punchDirectionY = deltaY * inverseDistance;
    }

    private void resolvePunchContact(
        BossComponent data,
        PositionComponent bossPosition,
        PositionComponent playerPosition
    ) {
        if (data.punchHit) {
            return;
        }
        float deltaX = playerPosition.x - bossPosition.x;
        float deltaY = playerPosition.y - bossPosition.y;
        if (deltaX * deltaX + deltaY * deltaY > PUNCH_HIT_RANGE_SQUARED) {
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
            ? data.punchDamage * (1f - defense.damageReduction)
            : data.punchDamage;
        health.current = Math.max(0f, health.current - damage);
        invulnerability.timeRemaining = invulnerability.duration;
        StatusEffectComponent status = player.getComponent(StatusEffectComponent.class);
        status.burningTime = Math.max(status.burningTime, 3f);
        data.punchHit = true;
        Gdx.app.log(
            "Boss",
            defense.blocking
                ? "Burning punch blocked: " + Math.round(damage) + " damage"
                : "Burning punch hit: " + Math.round(damage) + " damage + BURNING"
        );
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
