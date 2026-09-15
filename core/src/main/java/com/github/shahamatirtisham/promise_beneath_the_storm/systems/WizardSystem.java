package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import static com.github.shahamatirtisham.promise_beneath_the_storm.components.WizardComponent.*;

/** Spell decisions layered over the unchanged ranged retreat/approach/strafe logic. */
public class WizardSystem extends IteratingSystem {
    private final Entity player;
    private final WizardSpellSystem spells;
    public WizardSystem(Entity player, WizardSpellSystem spells) {
        super(Family.all(WizardComponent.class, RangedEnemyComponent.class, AnimationComponent.class,
            EnemyAIComponent.class, HealthComponent.class, PositionComponent.class, VelocityComponent.class).get());
        this.player = player;
        this.spells = spells;
    }
    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        WizardComponent wizard = enemy.getComponent(WizardComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        AnimationComponent animation = enemy.getComponent(AnimationComponent.class);
        wizard.attack01CooldownRemaining = Math.max(0f, wizard.attack01CooldownRemaining - deltaTime);
        wizard.attack02CooldownRemaining = Math.max(0f, wizard.attack02CooldownRemaining - deltaTime);
        if (enemy.getComponent(HealthComponent.class).current <= 0f || ai.state == EnemyAIComponent.State.DEAD) return;
        if (ai.state == EnemyAIComponent.State.STUNNED || animation.hurtTimeRemaining > 0f) return;
        if (wizard.isCasting()) {
            stop(enemy);
            ai.state = EnemyAIComponent.State.ATTACK;
            if (animation.state != AnimationComponent.State.ATTACK) {
                animation.state = AnimationComponent.State.ATTACK;
                animation.stateTime = wizard.castAnimationTime;
                return;
            }
            if (animation.attack.isAnimationFinished(animation.stateTime) && wizard.lastFrameShown) {
                wizard.state = WizardComponent.State.RECOVERY;
                wizard.recoveryRemaining = CAST_RECOVERY_DURATION;
                ai.state = EnemyAIComponent.State.RECOVER;
                return;
            }
            int currentFrame = animation.attack.getKeyFrameIndex(animation.stateTime);
            float nextTime = animation.stateTime + deltaTime;
            if (animation.attack.getKeyFrameIndex(nextTime) > currentFrame + 1)
                nextTime = Math.nextUp((currentFrame + 1) * animation.attack.getFrameDuration());
            wizard.castAnimationTime = nextTime;
            if (!wizard.effectReleased && animation.attack.getKeyFrameIndex(nextTime) >= CAST_RELEASE_FRAME) {
                wizard.effectReleased = true;
                PositionComponent position = enemy.getComponent(PositionComponent.class);
                float damage = ai.attackDamage;
                if (wizard.state == WizardComponent.State.CAST_ATTACK01)
                    spells.spawnCrystal(wizard.attack01TargetX, wizard.attack01TargetY,
                        position.x, position.y, damage * ATTACK01_DAMAGE);
                else spells.spawnFireball(position.x, position.y, wizard.facingX, wizard.facingY,
                    damage * FIREBALL_DAMAGE);
            }
            return;
        }
        if (wizard.state == WizardComponent.State.RECOVERY) {
            stop(enemy);
            wizard.recoveryRemaining = Math.max(0f, wizard.recoveryRemaining - deltaTime);
            if (wizard.recoveryRemaining == 0f) {
                wizard.state = WizardComponent.State.NONE;
                ai.state = EnemyAIComponent.State.CHASE;
            }
            return;
        }
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead || playerState.controlsLocked) return;
        PositionComponent position = enemy.getComponent(PositionComponent.class);
        PositionComponent target = player.getComponent(PositionComponent.class);
        float dx = target.x - position.x, dy = target.y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (dx != 0f) animation.facingLeft = dx < 0f;
        float retreat = Math.max(WIZARD_RETREAT_DISTANCE,
            enemy.getComponent(RangedEnemyComponent.class).preferredMinimumRange);
        if (distance < retreat) return;
        boolean crystal = distance >= ATTACK01_MIN_RANGE && distance <= ATTACK01_MAX_RANGE
            && wizard.attack01CooldownRemaining <= 0f;
        // Fireballs are exclusively long range; a cooling crystal never falls back to fireball nearby.
        boolean fireball = distance > ATTACK01_MAX_RANGE && distance <= ATTACK02_MAX_RANGE
            && wizard.attack02CooldownRemaining <= 0f;
        if (!crystal && !fireball) return;
        wizard.state = crystal ? WizardComponent.State.CAST_ATTACK01 : WizardComponent.State.CAST_ATTACK02;
        if (crystal) {
            wizard.attack01TargetX = target.x;
            wizard.attack01TargetY = target.y;
            wizard.attack01CooldownRemaining = ATTACK01_COOLDOWN;
        } else wizard.attack02CooldownRemaining = ATTACK02_COOLDOWN;
        wizard.facingX = dx / distance;
        wizard.facingY = dy / distance;
        animation.facingLeft = wizard.facingX < 0f;
        wizard.castAnimationTime = 0f;
        wizard.effectReleased = wizard.lastFrameShown = false;
        animation.attack = animation.attackVariants[crystal ? 0 : 1];
        animation.state = AnimationComponent.State.ATTACK;
        animation.stateTime = 0f;
        ai.state = EnemyAIComponent.State.ATTACK;
        ai.attackPending = false;
        stop(enemy);
    }
    private void stop(Entity enemy) {
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        velocity.vx = velocity.vy = 0f;
    }
}
