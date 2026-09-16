package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Presentation only. Combat, movement, damage and phase changes remain boss-system owned. */
public class IrhosAnimationComponent implements Component {
    public static final float IRHOS_RENDER_SIZE = 4.2f;
    public static final float IRHOS_RENDER_Y_OFFSET = 1.9f;

    // Ordinals are the supplied sheet rows. Never flip these regions.
    public enum Direction { DOWN, LEFT, RIGHT, UP }
    public enum Action {
        Idle, Walk, SlamWindup, Slam, SlamRecovery, RushWindup, Rush, RushRecovery,
        VolleyWindup, Volley, VolleyRelease, VolleyRecovery, Hurt, Transition, Reveal,
        Death, Corpse
    }

    public BossComponent.Phase form = BossComponent.Phase.IRON_FIST;
    public BossComponent.Phase previousPhase = BossComponent.Phase.IRON_FIST;
    public BossComponent.Phase transitionFrom = BossComponent.Phase.IRON_FIST;
    public BossComponent.AttackState previousGameplayState;
    public Action action = Action.Idle;
    public Direction direction = Direction.DOWN;
    public Direction lockedAttackDirection = Direction.DOWN;
    public float elapsed;
    public float stateDuration;
    public float transitionDuration;
    public boolean wasTransitioning;
    public float previousHealth = BossComponent.MAX_HEALTH;
    public float hurtTimeRemaining;
    public boolean deathStarted;
    public float deathElapsed;
    public TextureRegion frame;

    public static Direction facing(float x, float y, Direction previous) {
        if (x == 0f && y == 0f) return previous;
        if (Math.abs(x) > Math.abs(y)) return x > 0f ? Direction.RIGHT : Direction.LEFT;
        return y > 0f ? Direction.UP : Direction.DOWN;
    }
}
