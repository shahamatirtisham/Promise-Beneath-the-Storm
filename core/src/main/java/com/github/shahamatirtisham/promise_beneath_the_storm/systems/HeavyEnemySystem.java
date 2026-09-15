package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;

/** Chooses Heavy attacks and queues frame-driven impacts for EnemyAttackSystem. */
public class HeavyEnemySystem extends IteratingSystem {
    private final Entity player;
    public HeavyEnemySystem(Entity player) {
        super(Family.all(HeavyEnemyComponent.class, EnemyAIComponent.class,
            AnimationComponent.class, PositionComponent.class, VelocityComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        HeavyEnemyComponent heavy = enemy.getComponent(HeavyEnemyComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        AnimationComponent animation = enemy.getComponent(AnimationComponent.class);
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        heavy.attack03CooldownRemaining = Math.max(0f, heavy.attack03CooldownRemaining - deltaTime);
        velocity.vx = velocity.vy = 0f;
        if (ai.state == EnemyAIComponent.State.DEAD
            || enemy.getComponent(HealthComponent.class).current <= 0f) return;
        if (ai.state == EnemyAIComponent.State.STUNNED) {
            ai.stateTimeRemaining -= deltaTime;
            if (ai.stateTimeRemaining > 0f) return;
            ai.state = heavy.attackCommitted ? EnemyAIComponent.State.ATTACK : EnemyAIComponent.State.CHASE;
        }
        if (animation.hurtTimeRemaining > 0f) return;
        if (heavy.attackCommitted) {
            ai.state = EnemyAIComponent.State.ATTACK;
            if (animation.state != AnimationComponent.State.ATTACK) {
                animation.state = AnimationComponent.State.ATTACK;
                animation.stateTime = heavy.attackAnimationTime;
                return;
            }
            int currentFrame = animation.attack.getKeyFrameIndex(animation.stateTime);
            // This is the same timeline EnemyAnimationSystem presents this update.
            float nextTime = animation.stateTime + deltaTime;
            if (animation.attack.getKeyFrameIndex(nextTime) > currentFrame + 1)
                nextTime = Math.nextUp((currentFrame + 1) * animation.attack.getFrameDuration());
            int frame = animation.attack.getKeyFrameIndex(nextTime);
            if (!heavy.firstHitResolved && (heavy.currentAttack == HeavyEnemyComponent.Attack.ATTACK01
                ? frame == 5 : heavy.currentAttack == HeavyEnemyComponent.Attack.ATTACK02
                    ? frame >= 3 && frame <= 4 : frame == 4)) {
                heavy.firstHitResolved = true;
                ai.attackPending = true;
            }
            if (heavy.currentAttack == HeavyEnemyComponent.Attack.ATTACK02
                && !heavy.secondHitResolved && frame >= 8 && frame <= 9) {
                heavy.secondHitResolved = true;
                ai.attackPending = true;
            }
            if (animation.attack.isAnimationFinished(animation.stateTime) && heavy.lastFrameShown) {
                heavy.attackCommitted = false;
                ai.state = EnemyAIComponent.State.RECOVER;
                ai.stateTimeRemaining = ai.recoveryDuration;
                return;
            }
            // At most one source frame per update: hitches cannot skip either hit window.
            heavy.attackAnimationTime = nextTime;
            return;
        }
        PlayerComponent state = player.getComponent(PlayerComponent.class);
        if (state.dead || state.controlsLocked) return;
        if (ai.state == EnemyAIComponent.State.RECOVER) {
            ai.stateTimeRemaining -= deltaTime;
            if (ai.stateTimeRemaining <= 0f) ai.state = EnemyAIComponent.State.CHASE;
            return;
        }
        PositionComponent target = player.getComponent(PositionComponent.class);
        PositionComponent position = enemy.getComponent(PositionComponent.class);
        float dx = target.x - position.x, dy = target.y - position.y;
        float distanceSquared = dx * dx + dy * dy;
        if (ai.state == EnemyAIComponent.State.IDLE) {
            if (distanceSquared <= ai.detectionRange * ai.detectionRange) ai.state = EnemyAIComponent.State.CHASE;
            return;
        }
        if (dx != 0f) animation.facingLeft = dx < 0f;
        boolean special = heavy.attack03CooldownRemaining <= 0f
            && distanceSquared <= ai.attackRange * ai.attackRange;
        if (special || distanceSquared <= HeavyEnemyComponent.MELEE_RANGE * HeavyEnemyComponent.MELEE_RANGE) {
            heavy.currentAttack = special ? HeavyEnemyComponent.Attack.ATTACK03 : heavy.nextMeleeVariant;
            if (special) heavy.attack03CooldownRemaining = HeavyEnemyComponent.ATTACK03_COOLDOWN;
            else heavy.nextMeleeVariant = heavy.currentAttack == HeavyEnemyComponent.Attack.ATTACK01
                ? HeavyEnemyComponent.Attack.ATTACK02 : HeavyEnemyComponent.Attack.ATTACK01;
            float length = (float) Math.sqrt(distanceSquared);
            heavy.facingX = length > 0f ? dx / length : (animation.facingLeft ? -1f : 1f);
            heavy.facingY = length > 0f ? dy / length : 0f;
            heavy.firstHitResolved = heavy.secondHitResolved = heavy.lastFrameShown = false;
            heavy.attackCommitted = true;
            heavy.attackAnimationTime = 0f;
            animation.attack = animation.attackVariants[heavy.currentAttack.ordinal()];
            animation.state = AnimationComponent.State.ATTACK;
            animation.stateTime = 0f;
            ai.attackPending = false;
            ai.state = EnemyAIComponent.State.ATTACK;
        } else if (distanceSquared > 0f) {
            float scale = ai.movementSpeed / (float) Math.sqrt(distanceSquared);
            velocity.vx = dx * scale;
            velocity.vy = dy * scale;
        }
    }
}
