package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Directional defense and temporary guard-break state. */
public class ShieldGuardComponent implements Component {
    public static final float ATTACK_HIT_PROGRESS = 0.70f;
    public float facingX = -1f;
    public float facingY;
    public float frontalDamageReduction = 1f;
    public float guardBrokenTimeRemaining;
    public float comboBreakDuration = 2.5f;
    public float parryBreakDuration = 3.5f;
    // Local visual events; these never change the gameplay AI or shield direction.
    public boolean blockVisualRequested;
    public boolean hitVisualRequested;
    public float blockVisualTimeRemaining;
    public int nextAttackAnimationIndex;
    public float attackAnimationTime;
    public float attackAnimationDuration;
    public boolean attackDamageQueued;
    public boolean attackLastFrameShown;
    public EnemyAIComponent.State previousAnimationAIState = EnemyAIComponent.State.IDLE;

    /** Called once by the AI when committing a new attack. */
    public void beginAttack(AnimationComponent animation) {
        animation.attack = animation.attackVariants[nextAttackAnimationIndex];
        nextAttackAnimationIndex = (nextAttackAnimationIndex + 1) % animation.attackVariants.length;
        attackAnimationTime = 0f;
        attackAnimationDuration = animation.attack.getAnimationDuration();
        attackDamageQueued = false;
        attackLastFrameShown = false;
        if (animation.state != AnimationComponent.State.HURT
            && animation.state != AnimationComponent.State.BLOCK) animation.stateTime = 0f;
    }

    public void requestBlockVisual() {
        if (!isGuardBroken()) blockVisualRequested = true;
    }

    public void requestHitVisual() {
        hitVisualRequested = true;
        blockVisualRequested = false;
        blockVisualTimeRemaining = 0f;
    }

    public boolean isGuardBroken() {
        return guardBrokenTimeRemaining > 0f;
    }
}
