package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Runtime state for the existing HEAVY archetype, presented as a Werebear. */
public class HeavyEnemyComponent implements Component {
    public static final float ATTACK03_COOLDOWN = 10f;
    public static final float MELEE_RANGE = 1.15f;
    public enum Attack { ATTACK01, ATTACK02, ATTACK03 }
    public Attack currentAttack;
    public Attack nextMeleeVariant = Attack.ATTACK01;
    public float attack03CooldownRemaining = 0f;
    // Saved animation timeline while a hurt reaction or stun is displayed.
    public float attackAnimationTime;
    public boolean attackCommitted;
    public boolean lastFrameShown;
    public boolean firstHitResolved;
    public boolean secondHitResolved;
    public float facingX = 1f;
    public float facingY;
    public Animation<TextureRegion> deathShadow;
    public TextureRegion shadow;
    public float deathShadowTime;
    public boolean deathStarted;
}
