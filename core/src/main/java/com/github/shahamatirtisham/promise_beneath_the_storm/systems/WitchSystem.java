package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;

/** Selects attacks before physics; EnemyAttackSystem resolves their hits after physics. */
public class WitchSystem extends IteratingSystem {
    private final Entity player;

    public WitchSystem(Entity player) {
        super(Family.all(WitchComponent.class, EnemyAIComponent.class,
            PositionComponent.class, VelocityComponent.class,
            PhysicsComponent.class, HealthComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        ai.attackPending = false; // Witch attacks use their own active windows.

        if (enemy.getComponent(HealthComponent.class).current <= 0f
            || ai.state == EnemyAIComponent.State.DEAD) {
            enterState(witch, WitchComponent.State.DEAD);
            return; // DeathSystem owns body deactivation and kill credit.
        }
        if (witch.state == WitchComponent.State.DEAD) {
            // The existing Necromancer can revive this enemy.
            enterState(witch, WitchComponent.State.CHASE);
        }
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead || playerState.controlsLocked
            || player.getComponent(HealthComponent.class).current <= 0f) {
            cancelAttack(witch);
            enterState(witch, WitchComponent.State.IDLE);
            ai.state = EnemyAIComponent.State.IDLE;
            return;
        }

        witch.chargeCooldownRemaining = Math.max(0f, witch.chargeCooldownRemaining - deltaTime);
        witch.sideAttackCooldownRemaining = Math.max(0f, witch.sideAttackCooldownRemaining - deltaTime);
        if (ai.state == EnemyAIComponent.State.STUNNED) {
            cancelAttack(witch);
            enterState(witch, WitchComponent.State.STUNNED);
            witch.stateTime += deltaTime;
            ai.stateTimeRemaining -= deltaTime;
            if (ai.stateTimeRemaining <= 0f) {
                enterState(witch, WitchComponent.State.CHASE);
                ai.state = EnemyAIComponent.State.CHASE;
            }
            return;
        }

        PositionComponent position = enemy.getComponent(PositionComponent.class);
        PositionComponent target = player.getComponent(PositionComponent.class);
        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float distanceSquared = dx * dx + dy * dy;
        if (distanceSquared > ai.detectionRange * ai.detectionRange) {
            cancelAttack(witch);
            enterState(witch, WitchComponent.State.IDLE);
            ai.state = EnemyAIComponent.State.IDLE;
            return;
        }
        if (witch.state == WitchComponent.State.IDLE) {
            enterState(witch, WitchComponent.State.CHASE);
        }
        witch.stateTime += deltaTime;

        if (witch.state == WitchComponent.State.SIDE_ATTACK_WINDUP
            || witch.state == WitchComponent.State.SIDE_ATTACK) {
            witch.advanceSideAttackAnimation(deltaTime);
        }

        switch (witch.state) {
            case CHASE:
                // Exactly this priority: available horizontal cast, nearby charge, chase.
                if (dx != 0f && Math.abs(dy) <= witch.sideAlignmentTolerance
                    && Math.abs(dx) <= witch.sideAttackRange
                    && witch.sideAttackCooldownRemaining <= 0f) {
                    witch.sideAttackLeft = dx < 0f;
                    witch.facingLeft = witch.sideAttackLeft;
                    witch.sideAttackHitPlayer = false;
                    witch.sideAttackDamageChecked = false;
                    witch.sideAttackDamagePending = false;
                    witch.sideAttackAnimationTime = 0f;
                    enterState(witch, WitchComponent.State.SIDE_ATTACK_WINDUP);
                } else if (distanceSquared <= witch.chargeActivationRange * witch.chargeActivationRange
                    && witch.chargeCooldownRemaining <= 0f) {
                    float inverseDistance = distanceSquared > 0f
                        ? 1f / (float) Math.sqrt(distanceSquared) : 0f;
                    witch.chargeDirectionX = distanceSquared > 0f
                        ? dx * inverseDistance : (witch.facingLeft ? -1f : 1f);
                    witch.chargeDirectionY = dy * inverseDistance;
                    if (witch.chargeDirectionX != 0f) {
                        witch.facingLeft = witch.chargeDirectionX < 0f;
                    }
                    witch.chargeHitPlayer = false;
                    enterState(witch, WitchComponent.State.CHARGE_WINDUP);
                } else if (distanceSquared > 0f) {
                    // Same normalized velocity convention as EnemyAISystem/ChargerSystem.
                    float speed = ai.movementSpeed / (float) Math.sqrt(distanceSquared);
                    velocity.vx = dx * speed;
                    velocity.vy = dy * speed;
                    if (dx != 0f) witch.facingLeft = dx < 0f;
                }
                break;
            case CHARGE_WINDUP:
                if (witch.stateTime >= witch.chargeWindup) {
                    enterState(witch, WitchComponent.State.CHARGING);
                }
                break;
            case CHARGING:
                if (witch.stateTime >= witch.chargeDuration || isChargingIntoWall(enemy)) {
                    enterState(witch, WitchComponent.State.CHARGE_RECOVERY);
                }
                break;
            case CHARGE_RECOVERY:
                if (witch.stateTime >= witch.chargeRecovery) {
                    witch.chargeCooldownRemaining = witch.chargeCooldown;
                    enterState(witch, WitchComponent.State.CHASE);
                }
                break;
            case SIDE_ATTACK_WINDUP:
                queueSideAttackDamage(witch);
                if (witch.stateTime >= witch.sideAttackWindup) {
                    enterState(witch, WitchComponent.State.SIDE_ATTACK);
                }
                break;
            case SIDE_ATTACK:
                queueSideAttackDamage(witch);
                if (witch.getSideAttackAnimationTime() >= witch.getSideAttackAnimationDuration()) {
                    enterState(witch, WitchComponent.State.SIDE_ATTACK_RECOVERY);
                }
                break;
            case SIDE_ATTACK_RECOVERY:
                if (witch.stateTime >= witch.sideAttackRecovery) {
                    witch.sideAttackCooldownRemaining = witch.sideAttackCooldown;
                    enterState(witch, WitchComponent.State.CHASE);
                }
                break;
            default:
                break;
        }
        if (witch.state == WitchComponent.State.CHARGING) {
            velocity.vx = witch.chargeDirectionX * witch.chargeSpeed;
            velocity.vy = witch.chargeDirectionY * witch.chargeSpeed;
        }
        switch (witch.state) {
            case IDLE: ai.state = EnemyAIComponent.State.IDLE; break;
            case CHASE: ai.state = EnemyAIComponent.State.CHASE; break;
            case CHARGE_RECOVERY:
            case SIDE_ATTACK_RECOVERY: ai.state = EnemyAIComponent.State.RECOVER; break;
            default: ai.state = EnemyAIComponent.State.ATTACK; break;
        }
    }

    private void queueSideAttackDamage(WitchComponent witch) {
        if (!witch.sideAttackDamageChecked
            && witch.getSideAttackAnimationFrame() >= WitchComponent.SIDE_ATTACK_DAMAGE_FRAME) {
            witch.sideAttackDamagePending = true;
        }
    }

    /** Contacts come from the previous fixed physics step; no coordinate clamping. */
    private boolean isChargingIntoWall(Entity enemy) {
        Body body = enemy.getComponent(PhysicsComponent.class).body;
        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        for (Contact contact : body.getWorld().getContactList()) {
            if (!contact.isTouching() || contact.getFixtureA().isSensor()
                || contact.getFixtureB().isSensor()) continue;
            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();
            Body other = bodyA == body ? bodyB : bodyB == body ? bodyA : null;
            if (other == null || other.getType()
                != com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody) continue;
            com.badlogic.gdx.math.Vector2 normal = contact.getWorldManifold().getNormal();
            float sign = bodyA == body ? 1f : -1f;
            if (sign * (normal.x * witch.chargeDirectionX + normal.y * witch.chargeDirectionY)
                > WitchComponent.WALL_IMPACT_DIRECTION_TOLERANCE) return true;
        }
        return false;
    }

    /** Called after physics, using real Box2D contact or the shared magic rectangle. */
    public static boolean overlapsPlayer(Entity enemy, Entity player, float playerRadius,
                                         Rectangle scratchBounds) {
        WitchComponent witch = enemy.getComponent(WitchComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (ai.state == EnemyAIComponent.State.STUNNED || ai.state == EnemyAIComponent.State.DEAD
            || playerState.dead || playerState.controlsLocked) return false;

        PositionComponent position = enemy.getComponent(PositionComponent.class);
        PositionComponent target = player.getComponent(PositionComponent.class);
        if (witch.state == WitchComponent.State.CHARGING && !witch.chargeHitPlayer) {
            Body body = enemy.getComponent(PhysicsComponent.class).body;
            Body playerBody = player.getComponent(PhysicsComponent.class).body;
            for (Contact contact : body.getWorld().getContactList()) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                if (contact.isTouching()
                    && ((bodyA == body && bodyB == playerBody)
                        || (bodyB == body && bodyA == playerBody))) return true;
            }
            float radius = WitchComponent.BODY_RADIUS + playerRadius;
            float dx = target.x - position.x;
            float dy = target.y - position.y;
            return dx * dx + dy * dy <= radius * radius;
        }
        if ((witch.state == WitchComponent.State.SIDE_ATTACK
                || witch.state == WitchComponent.State.SIDE_ATTACK_WINDUP
                || witch.state == WitchComponent.State.SIDE_ATTACK_RECOVERY)
            && witch.sideAttackDamagePending && !witch.sideAttackHitPlayer) {
            Rectangle bounds = witch.getSideAttackBounds(position, scratchBounds);
            float dx = target.x - MathUtils.clamp(target.x, bounds.x, bounds.x + bounds.width);
            float dy = target.y - MathUtils.clamp(target.y, bounds.y, bounds.y + bounds.height);
            return dx * dx + dy * dy <= playerRadius * playerRadius;
        }
        return false;
    }

    /** A resolved block/parry also consumes the cast; invulnerable overlaps do not. */
    public static void markAttackResolved(WitchComponent witch) {
        if (witch.state == WitchComponent.State.CHARGING) witch.chargeHitPlayer = true;
        if (witch.sideAttackDamagePending) witch.sideAttackHitPlayer = true;
    }

    private void cancelAttack(WitchComponent witch) {
        witch.sideAttackDamagePending = false;
        switch (witch.state) {
            case CHARGE_WINDUP:
            case CHARGING:
            case CHARGE_RECOVERY:
                witch.chargeCooldownRemaining = witch.chargeCooldown;
                break;
            case SIDE_ATTACK_WINDUP:
            case SIDE_ATTACK:
            case SIDE_ATTACK_RECOVERY:
                witch.sideAttackCooldownRemaining = witch.sideAttackCooldown;
                break;
            default: break;
        }
    }

    private void enterState(WitchComponent witch, WitchComponent.State state) {
        if (witch.state == state) return;
        witch.state = state;
        witch.stateTime = 0f;
        if (Gdx.app != null) {
            Gdx.app.log("Witch", state + (state == WitchComponent.State.SIDE_ATTACK_WINDUP
                ? (witch.sideAttackLeft ? " left" : " right") : ""));
        }
    }
}
