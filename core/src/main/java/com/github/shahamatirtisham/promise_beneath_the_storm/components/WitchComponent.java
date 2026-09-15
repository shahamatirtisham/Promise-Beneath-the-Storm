package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;

/** Gameplay states and tuning; no sprite assets are required. Times are seconds. */
public class WitchComponent implements Component {
    public static final float BODY_RADIUS = 0.45f;
    public static final float DETECTION_RANGE = 8f;
    public static final float CHARGE_ACTIVATION_RANGE = 5f;
    public static final float CHARGE_SPEED = 7.5f;
    public static final float CHARGE_WINDUP = 0.55f;
    public static final float CHARGE_DURATION = 0.65f;
    public static final float CHARGE_RECOVERY = 0.65f;
    public static final float CHARGE_COOLDOWN = 3f;
    public static final float CHARGE_DAMAGE_MULTIPLIER = 1.2f;
    public static final float CHARGE_ANIMATION_FRAME_DURATION = 0.07f;
    public static final int ATTACK_ANIMATION_FRAME_COUNT = 9;
    public static final int SIDE_ATTACK_DAMAGE_FRAME = 7;
    // Preserve the existing cast's visual speed; recovery now follows the full cast.
    public static final float ATTACK_ANIMATION_FRAME_DURATION = 1.3f / ATTACK_ANIMATION_FRAME_COUNT;
    public static final float WALL_IMPACT_DIRECTION_TOLERANCE = 0.01f;
    public static final float SIDE_ATTACK_RANGE = 5f;
    public static final float SIDE_ALIGNMENT_TOLERANCE = 0.9f;
    public static final float SIDE_ATTACK_HEIGHT = 1.4f;
    public static final float SIDE_ATTACK_WINDUP = 0.5f;
    public static final float SIDE_ATTACK_DURATION =
        ATTACK_ANIMATION_FRAME_COUNT * ATTACK_ANIMATION_FRAME_DURATION - SIDE_ATTACK_WINDUP;
    public static final float SIDE_ATTACK_RECOVERY = 0.5f;
    public static final float SIDE_ATTACK_COOLDOWN = 2.5f;
    public static final float SIDE_DAMAGE_MULTIPLIER = 1f;

    public enum State {
        IDLE,
        CHASE,
        CHARGE_WINDUP,
        CHARGING,
        CHARGE_RECOVERY,
        SIDE_ATTACK_WINDUP,
        SIDE_ATTACK,
        SIDE_ATTACK_RECOVERY,
        STUNNED,
        DEAD
    }

    public State state = State.IDLE;
    public float stateTime;
    public boolean facingLeft;
    public float chargeDirectionX = 1f;
    public float chargeDirectionY;
    public boolean sideAttackLeft;
    public boolean chargeHitPlayer;
    public boolean sideAttackHitPlayer;
    // A missed/invulnerable damage opportunity is also consumed, once per cast.
    public boolean sideAttackDamageChecked;
    public boolean sideAttackDamagePending;
    public float sideAttackAnimationTime;
    public float chargeCooldownRemaining;
    public float sideAttackCooldownRemaining;

    public float chargeActivationRange = CHARGE_ACTIVATION_RANGE;
    public float chargeSpeed = CHARGE_SPEED;
    public float chargeWindup = CHARGE_WINDUP;
    public float chargeDuration = CHARGE_DURATION;
    public float chargeRecovery = CHARGE_RECOVERY;
    public float chargeCooldown = CHARGE_COOLDOWN;
    public float chargeDamageMultiplier = CHARGE_DAMAGE_MULTIPLIER;
    public float sideAttackRange = SIDE_ATTACK_RANGE;
    public float sideAlignmentTolerance = SIDE_ALIGNMENT_TOLERANCE;
    public float sideAttackHeight = SIDE_ATTACK_HEIGHT;
    public float sideAttackWindup = SIDE_ATTACK_WINDUP;
    public float sideAttackDuration = SIDE_ATTACK_DURATION;
    public float sideAttackRecovery = SIDE_ATTACK_RECOVERY;
    public float sideAttackCooldown = SIDE_ATTACK_COOLDOWN;
    public float sideDamageMultiplier = SIDE_DAMAGE_MULTIPLIER;

    /** One continuous clock shared by the sprite and the damage opportunity. */
    public float getSideAttackAnimationTime() {
        return sideAttackAnimationTime;
    }

    public int getSideAttackAnimationFrame() {
        return Math.min(ATTACK_ANIMATION_FRAME_COUNT - 1,
            (int) (getSideAttackAnimationTime() / ATTACK_ANIMATION_FRAME_DURATION + 0.00001f));
    }

    public float getSideAttackAnimationDuration() {
        return ATTACK_ANIMATION_FRAME_COUNT * ATTACK_ANIMATION_FRAME_DURATION;
    }

    public void advanceSideAttackAnimation(float deltaTime) {
        float nextTime = sideAttackAnimationTime + deltaTime * getSideAttackAnimationDuration()
            / Math.max(0.001f, sideAttackWindup + sideAttackDuration);
        float damageFrameTime = SIDE_ATTACK_DAMAGE_FRAME * ATTACK_ANIMATION_FRAME_DURATION;
        float finalFrameTime = (ATTACK_ANIMATION_FRAME_COUNT - 1) * ATTACK_ANIMATION_FRAME_DURATION;
        // Display the damage and finishing frames for at least one update even
        // during a stall; keep this same clock for the sprite and hit resolution.
        if (sideAttackAnimationTime < damageFrameTime && nextTime >= damageFrameTime) {
            nextTime = damageFrameTime;
        } else if (sideAttackAnimationTime < finalFrameTime && nextTime >= finalFrameTime) {
            nextTime = finalFrameTime;
        }
        sideAttackAnimationTime = nextTime;
    }

    /** Shared by collision and rendering. Range is measured from the body edge. */
    public Rectangle getSideAttackBounds(PositionComponent position, Rectangle out) {
        float x = sideAttackLeft
            ? position.x - BODY_RADIUS - sideAttackRange
            : position.x + BODY_RADIUS;
        return out.set(x, position.y - sideAttackHeight / 2f,
            sideAttackRange, sideAttackHeight);
    }
}
