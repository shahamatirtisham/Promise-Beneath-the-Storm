package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Spell runtime and centralized tuning for the normal RANGED archetype. */
public class WizardComponent implements Component {
    public static final float WIZARD_RENDER_SIZE = 4.8f;
    public static final float WIZARD_RENDER_Y_OFFSET = 2.4f;
    // Match the old ranged minimum: do not cast while the old AI would retreat.
    public static final float WIZARD_RETREAT_DISTANCE = 3.2f;
    public static final float ATTACK01_MIN_RANGE = 3.2f;
    public static final float ATTACK01_MAX_RANGE = 5f;
    public static final float ATTACK01_BLAST_RADIUS = 1.2f;
    // Multipliers retain room/level-scaled EnemyAIComponent.attackDamage.
    public static final float ATTACK01_DAMAGE = 1f;
    public static final float ATTACK01_COOLDOWN = 5f;
    public static final float ATTACK02_MAX_RANGE = 8f;
    public static final float ATTACK02_COOLDOWN = 2f;
    public static final float FIREBALL_SPEED = 5f;
    public static final float FIREBALL_TURN_RATE = 240f; // degrees per second
    public static final float FIREBALL_HOMING_DURATION = 1.25f;
    public static final float FIREBALL_TARGET_LEAD_TIME = 0.2f;
    public static final float FIREBALL_DAMAGE = 1f;
    public static final float FIREBALL_STUN_DURATION = 0.45f;
    public static final float FIREBALL_MAX_LIFETIME = 6f;
    public static final float FIREBALL_RADIUS = 0.18f;
    public static final float FIREBALL_RENDER_SIZE = 3.2f;
    public static final float CRYSTAL_RENDER_SIZE = 3.6f;
    public static final float WIZARD_IDLE_FRAME_DURATION = 0.14f;
    public static final float WIZARD_WALK_FRAME_DURATION = 0.11f;
    public static final float WIZARD_ATTACK01_FRAME_DURATION = 0.12f;
    public static final float WIZARD_ATTACK02_FRAME_DURATION = 0.12f;
    public static final float WIZARD_HURT_FRAME_DURATION = 0.10f;
    public static final float WIZARD_DEATH_FRAME_DURATION = 0.14f;
    public static final float CRYSTAL_EFFECT_FRAME_DURATION = 0.12f;
    public static final float FIREBALL_EFFECT_FRAME_DURATION = 0.10f;
    public static final float CAST_RECOVERY_DURATION = 0.35f;
    public static final int CAST_RELEASE_FRAME = 4;
    public static final int CRYSTAL_DAMAGE_FRAME = 6;
    public static final float PLAYER_RADIUS = 0.4f;
    public enum State { NONE, CAST_ATTACK01, CAST_ATTACK02, RECOVERY }
    public State state = State.NONE;
    public float attack01CooldownRemaining;
    public float attack02CooldownRemaining = 0.75f;
    public float attack01TargetX;
    public float attack01TargetY;
    public float facingX = 1f;
    public float facingY;
    public float castAnimationTime;
    public float recoveryRemaining;
    public boolean effectReleased;
    public boolean lastFrameShown;
    public boolean deathStarted;
    public boolean isCasting() {
        return state == State.CAST_ATTACK01 || state == State.CAST_ATTACK02;
    }
}
