package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Tunable state for the low-threat Iron Fist support minions. */
public class SkeletonComponent implements Component {
    public static final float BODY_RADIUS = 0.38f;
    public static final float MAX_HEALTH = 25f;
    public static final float ATTACK_DAMAGE = 5f;
    public static final float MOVEMENT_SPEED = 1.75f;
    public static final float ATTACK_RANGE = 0.78f;
    public static final float ATTACK_COOLDOWN = 0.9f;
    public static final float SUMMON_SPACING = 2.15f;
    public static final float SEPARATION_DISTANCE = 0.9f;
    public static final float RENDER_SIZE = 2.5f;
    public static final float RENDER_Y_OFFSET = 0.85f;
    public static final float ATTACK_HIT_PROGRESS = 0.58f;
    public static final int INITIAL_PLAYER_ATTACK_BLOCKS = 2;

    public boolean summoning = true;
    public float summonElapsed;
    public boolean facingLeft;
    public int attackVariant;
    public float attackElapsed;
    public boolean attackDamageQueued;
    public float previousHealth = MAX_HEALTH;
    public float hurtTimeRemaining;
    public int playerAttackBlocksRemaining = INITIAL_PLAYER_ATTACK_BLOCKS;
    public float blockTimeRemaining;
    public boolean blockVisualRequested;
    public boolean deathStarted;
}
