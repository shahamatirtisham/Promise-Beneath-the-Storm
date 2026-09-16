package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent.AttackState;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent.Phase;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.IrhosAnimationComponent.Action;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.IrhosAnimationResources;

/** Observes resolved combat/phase/death state. Writes only to IrhosAnimationComponent. */
public final class IrhosAnimationSystem extends IteratingSystem {
    public static final float DEATH_DURATION = 0.9f;
    public static final float HURT_DURATION = 0.36f;
    // Both full action sheets start with two anticipation poses. Gameplay has already
    // resolved by recovery entry, so begin at column 2 (the third/impact pose).
    private static final int IMPACT_FRAME = 2;
    private static final float ACTION_RECOVERY_FRACTION = 0.65f;
    private final IrhosAnimationResources resources;

    public IrhosAnimationSystem(IrhosAnimationResources resources) {
        super(Family.all(BossComponent.class, IrhosAnimationComponent.class,
            EnemyAIComponent.class, HealthComponent.class, PositionComponent.class,
            VelocityComponent.class).get());
        this.resources = resources;
    }

    @Override protected void processEntity(Entity entity, float delta) {
        BossComponent boss = entity.getComponent(BossComponent.class);
        IrhosAnimationComponent visual = entity.getComponent(IrhosAnimationComponent.class);
        float hp = entity.getComponent(HealthComponent.class).current;
        boolean damaged = hp < visual.previousHealth;
        visual.previousHealth = hp;
        if (hp <= 0f || entity.getComponent(EnemyAIComponent.class).state == EnemyAIComponent.State.DEAD) {
            if (!visual.deathStarted) {
                visual.deathStarted = true;
                visual.deathElapsed = 0f;
                visual.hurtTimeRemaining = 0f;
            } else visual.deathElapsed += delta;
            Action deathAction = visual.deathElapsed < DEATH_DURATION ? Action.Death : Action.Corpse;
            select(visual, Phase.IRHOS_REVEALED, deathAction, delta);
            visual.frame = resources.get(visual.form, visual.action).atProgress(
                visual.direction, visual.deathElapsed / DEATH_DURATION, 0);
            return;
        }

        if (boss.phase != visual.previousPhase) {
            visual.transitionFrom = visual.previousPhase;
            visual.previousPhase = boss.phase;
            visual.previousGameplayState = null;
        }
        if (boss.isTransitioning()) {
            if (!visual.wasTransitioning) visual.transitionDuration = boss.transitionTimeRemaining;
            visual.wasTransitioning = true;
            visual.hurtTimeRemaining = 0f;
            Action action = visual.transitionFrom == Phase.DEVILS_CROWN ? Action.Reveal : Action.Transition;
            select(visual, visual.transitionFrom, action, delta);
            finite(visual, progress(boss.transitionTimeRemaining, visual.transitionDuration), 0);
            return;
        }
        if (visual.wasTransitioning) visual.previousGameplayState = null;
        visual.wasTransitioning = false;

        AttackState state = gameplayState(boss);
        float remaining = boss.phase == Phase.IRHOS_REVEALED
            ? boss.revealedTimeRemaining : boss.attackTimeRemaining;
        if (state != visual.previousGameplayState) {
            // Each gameplay state installs its original timer before we observe it.
            // Capture it rather than duplicating combat tuning constants here.
            visual.stateDuration = remaining;
            PositionComponent position = entity.getComponent(PositionComponent.class);
            switch (state) {
                case SLAM_WINDUP:
                    visual.lockedAttackDirection = IrhosAnimationComponent.facing(
                        boss.slamTargetX - position.x, boss.slamTargetY - position.y, visual.direction);
                    break;
                case FLAME_PUNCH_WINDUP:
                    visual.lockedAttackDirection = IrhosAnimationComponent.facing(
                        boss.punchDirectionX, boss.punchDirectionY, visual.direction);
                    break;
                case CROWN_WINDUP:
                    visual.lockedAttackDirection = IrhosAnimationComponent.facing(
                        boss.crownFacingX, boss.crownFacingY, visual.direction);
                    break;
                default: break;
            }
            visual.previousGameplayState = state;
        }

        visual.hurtTimeRemaining = Math.max(0f, visual.hurtTimeRemaining - delta);
        if (damaged) visual.hurtTimeRemaining = HURT_DURATION;
        float progress = progress(remaining, visual.stateDuration);
        if (isCommitted(state)) {
            visual.direction = visual.lockedAttackDirection;
            // A hit never hides a telegraph, release, dash or recovery. Do not queue
            // stale hurt poses for later; the combat state always remains untouched.
            visual.hurtTimeRemaining = 0f;
            switch (state) {
                case SLAM_WINDUP: show(visual, boss.phase, Action.SlamWindup, progress, 0, delta); break;
                case SLAM_RECOVERY: recovery(visual, boss.phase, Action.Slam, Action.SlamRecovery, progress, delta); break;
                case FLAME_PUNCH_WINDUP: show(visual, boss.phase, Action.RushWindup, progress, 0, delta); break;
                case FLAME_PUNCH_DASH: show(visual, boss.phase, Action.Rush, progress, 0, delta); break;
                case FLAME_PUNCH_RECOVERY: show(visual, boss.phase, Action.RushRecovery, progress, 0, delta); break;
                case CROWN_WINDUP: show(visual, boss.phase, Action.VolleyWindup, progress, 0, delta); break;
                case CROWN_RECOVERY:
                    recovery(visual, boss.phase, boss.phase == Phase.IRHOS_REVEALED
                        ? Action.VolleyRelease : Action.Volley, Action.VolleyRecovery, progress, delta);
                    break;
                default: break;
            }
            return;
        }

        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        boolean moving = velocity.vx * velocity.vx + velocity.vy * velocity.vy > 0.0001f;
        if (moving) visual.direction = IrhosAnimationComponent.facing(velocity.vx, velocity.vy, visual.direction);
        if (visual.hurtTimeRemaining > 0f) {
            show(visual, boss.phase, Action.Hurt, 1f - visual.hurtTimeRemaining / HURT_DURATION, 0, delta);
        } else {
            select(visual, boss.phase, moving ? Action.Walk : Action.Idle, delta);
            visual.frame = resources.get(visual.form, visual.action).atTime(
                visual.direction, visual.elapsed, moving ? 0.12f : 0.16f);
        }
    }

    private void recovery(IrhosAnimationComponent visual, Phase form, Action impact,
        Action recovery, float progress, float delta) {
        if (progress < ACTION_RECOVERY_FRACTION) {
            show(visual, form, impact, progress / ACTION_RECOVERY_FRACTION,
                impact == Action.VolleyRelease ? 0 : IMPACT_FRAME, delta);
        } else {
            show(visual, form, recovery, (progress - ACTION_RECOVERY_FRACTION)
                / (1f - ACTION_RECOVERY_FRACTION), 0, delta);
        }
    }

    private void show(IrhosAnimationComponent visual, Phase form, Action action,
        float progress, int firstFrame, float delta) {
        select(visual, form, action, delta);
        finite(visual, progress, firstFrame);
    }

    private void finite(IrhosAnimationComponent visual, float progress, int firstFrame) {
        visual.frame = resources.get(visual.form, visual.action).atProgress(visual.direction, progress, firstFrame);
    }

    private static void select(IrhosAnimationComponent visual, Phase form, Action action, float delta) {
        if (visual.form != form || visual.action != action) visual.elapsed = 0f;
        else visual.elapsed += delta;
        visual.form = form;
        visual.action = action;
    }

    private static float progress(float remaining, float duration) {
        return duration <= 0f ? 1f : Math.max(0f, Math.min(1f, 1f - remaining / duration));
    }

    private static boolean isCommitted(AttackState state) {
        return state != AttackState.PURSUIT && state != AttackState.BURNING_PURSUIT
            && state != AttackState.CROWN_PURSUIT && state != AttackState.REVEALED_PURSUIT;
    }

    private static AttackState gameplayState(BossComponent boss) {
        // PhaseSystem runs after combat. On the update that ends a transformation,
        // combat has not yet initialized the new phase's attack state.
        if (!boss.attackCycleReady) return AttackState.PURSUIT;
        if (boss.attackState == AttackState.IRON_WIZARD_CAST
            || boss.attackState == AttackState.IRON_WIZARD_SEQUENCE
            || boss.attackState == AttackState.IRON_SKELETON_SUMMON) {
            return AttackState.PURSUIT;
        }
        if (boss.phase != Phase.IRHOS_REVEALED) return boss.attackState;
        // RevealedState changes on the actual resolveSlam/fireCrownVolley update;
        // its legacy AttackState mirror may still be WINDUP until the next update.
        switch (boss.revealedState) {
            case SLAM_WINDUP: return AttackState.SLAM_WINDUP;
            case SLAM_RECOVERY: return AttackState.SLAM_RECOVERY;
            case PUNCH_WINDUP: return AttackState.FLAME_PUNCH_WINDUP;
            case PUNCH_DASH: return AttackState.FLAME_PUNCH_DASH;
            case PUNCH_RECOVERY: return AttackState.FLAME_PUNCH_RECOVERY;
            case CROWN_WINDUP: return AttackState.CROWN_WINDUP;
            case CROWN_RECOVERY: return AttackState.CROWN_RECOVERY;
            default: return AttackState.REVEALED_PURSUIT;
        }
    }
}
