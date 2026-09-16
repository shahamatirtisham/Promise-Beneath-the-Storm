package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;

/** Simple chase/melee behavior and finite summon/death lifecycle for Irhos's support minions. */
public final class SkeletonSystem extends IteratingSystem {
    private final Entity player;
    private final World world;
    private final Array<Entity> enemies;
    private Engine engine;

    public SkeletonSystem(Entity player, World world, Array<Entity> enemies) {
        super(Family.all(SkeletonComponent.class, EnemyAIComponent.class,
            AnimationComponent.class, HealthComponent.class, PositionComponent.class,
            VelocityComponent.class, PhysicsComponent.class).get());
        this.player = player;
        this.world = world;
        this.enemies = enemies;
    }

    @Override public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
    }

    @Override protected void processEntity(Entity skeleton, float delta) {
        SkeletonComponent data = skeleton.getComponent(SkeletonComponent.class);
        EnemyAIComponent ai = skeleton.getComponent(EnemyAIComponent.class);
        AnimationComponent animation = skeleton.getComponent(AnimationComponent.class);
        HealthComponent health = skeleton.getComponent(HealthComponent.class);
        VelocityComponent velocity = skeleton.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;

        if (health.current <= 0f || ai.state == EnemyAIComponent.State.DEAD) {
            showDeath(skeleton, data, animation, delta);
            return;
        }
        if (data.summoning) {
            data.summonElapsed += delta;
            setVisual(animation, AnimationComponent.State.SUMMON, data.summonElapsed);
            if (data.summonElapsed >= animation.summon.getAnimationDuration()) {
                data.summoning = false;
                skeleton.getComponent(PhysicsComponent.class).body.setActive(true);
                ai.state = EnemyAIComponent.State.CHASE;
                setVisual(animation, AnimationComponent.State.IDLE, 0f);
            }
            return;
        }

        float currentHealth = health.current;
        if (currentHealth < data.previousHealth) {
            data.hurtTimeRemaining = animation.hurt.getAnimationDuration();
        }
        data.previousHealth = currentHealth;
        if (data.blockVisualRequested) {
            data.blockVisualRequested = false;
            data.blockTimeRemaining = animation.block.getAnimationDuration();
        }
        if (data.blockTimeRemaining > 0f) {
            data.blockTimeRemaining = Math.max(0f, data.blockTimeRemaining - delta);
            setVisual(animation, AnimationComponent.State.BLOCK,
                animation.block.getAnimationDuration() - data.blockTimeRemaining);
            return;
        }
        if (data.hurtTimeRemaining > 0f) {
            data.hurtTimeRemaining = Math.max(0f, data.hurtTimeRemaining - delta);
            setVisual(animation, AnimationComponent.State.HURT,
                animation.hurt.getAnimationDuration() - data.hurtTimeRemaining);
            // Hurt is visual only. The AI timers below remain intact on later frames.
            return;
        }

        PositionComponent position = skeleton.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float dx = playerPosition.x - position.x;
        float dy = playerPosition.y - position.y;
        float distance2 = dx * dx + dy * dy;
        if (dx != 0f) data.facingLeft = dx < 0f;

        switch (ai.state) {
            case IDLE:
                ai.state = EnemyAIComponent.State.CHASE;
                setVisual(animation, AnimationComponent.State.IDLE, animation.stateTime + delta);
                break;
            case CHASE:
                if (distance2 <= ai.attackRange * ai.attackRange) {
                    beginAttack(data, ai, animation);
                } else {
                    moveWithSeparation(skeleton, position, velocity, dx, dy, distance2);
                    setVisual(animation, AnimationComponent.State.WALK, animation.stateTime + delta);
                }
                break;
            case ATTACK:
                updateAttack(data, ai, animation, delta);
                break;
            case RECOVER:
                ai.stateTimeRemaining -= delta;
                setVisual(animation, AnimationComponent.State.IDLE, animation.stateTime + delta);
                if (ai.stateTimeRemaining <= 0f) ai.state = EnemyAIComponent.State.CHASE;
                break;
            case STUNNED:
                ai.stateTimeRemaining -= delta;
                setVisual(animation, AnimationComponent.State.HURT, animation.stateTime + delta);
                if (ai.stateTimeRemaining <= 0f) ai.state = EnemyAIComponent.State.CHASE;
                break;
            default:
                break;
        }
        animation.facingLeft = data.facingLeft;
    }

    private void beginAttack(SkeletonComponent data, EnemyAIComponent ai,
        AnimationComponent animation) {
        data.attackVariant = (data.attackVariant + 1) % 2;
        animation.attack = animation.attackVariants[data.attackVariant];
        data.attackElapsed = 0f;
        data.attackDamageQueued = false;
        ai.attackPending = false;
        ai.state = EnemyAIComponent.State.ATTACK;
        setVisual(animation, AnimationComponent.State.ATTACK, 0f);
    }

    private void updateAttack(SkeletonComponent data, EnemyAIComponent ai,
        AnimationComponent animation, float delta) {
        data.attackElapsed += delta;
        float duration = animation.attack.getAnimationDuration();
        if (!data.attackDamageQueued
            && data.attackElapsed >= duration * SkeletonComponent.ATTACK_HIT_PROGRESS) {
            data.attackDamageQueued = true;
            ai.attackPending = true;
        }
        setVisual(animation, AnimationComponent.State.ATTACK, data.attackElapsed);
        if (data.attackElapsed >= duration) {
            ai.state = EnemyAIComponent.State.RECOVER;
            ai.stateTimeRemaining = SkeletonComponent.ATTACK_COOLDOWN;
        }
    }

    private void moveWithSeparation(Entity self, PositionComponent position,
        VelocityComponent velocity, float dx, float dy, float distance2) {
        float inverse = distance2 == 0f ? 0f : 1f / (float) Math.sqrt(distance2);
        float moveX = dx * inverse;
        float moveY = dy * inverse;
        for (Entity other : getEntities()) {
            if (other == self || other.getComponent(HealthComponent.class).current <= 0f) continue;
            SkeletonComponent otherData = other.getComponent(SkeletonComponent.class);
            if (otherData.summoning) continue;
            PositionComponent otherPosition = other.getComponent(PositionComponent.class);
            float awayX = position.x - otherPosition.x;
            float awayY = position.y - otherPosition.y;
            float separation2 = awayX * awayX + awayY * awayY;
            if (separation2 > 0f && separation2 < SkeletonComponent.SEPARATION_DISTANCE
                * SkeletonComponent.SEPARATION_DISTANCE) {
                float inverseSeparation = 1f / (float) Math.sqrt(separation2);
                moveX += awayX * inverseSeparation * 0.75f;
                moveY += awayY * inverseSeparation * 0.75f;
            }
        }
        float length2 = moveX * moveX + moveY * moveY;
        if (length2 == 0f) return;
        float normalize = SkeletonComponent.MOVEMENT_SPEED / (float) Math.sqrt(length2);
        velocity.vx = moveX * normalize;
        velocity.vy = moveY * normalize;
    }

    private void showDeath(Entity skeleton, SkeletonComponent data,
        AnimationComponent animation, float delta) {
        if (!data.deathStarted) {
            data.deathStarted = true;
            animation.state = AnimationComponent.State.DEAD;
            animation.stateTime = 0f;
        } else {
            animation.stateTime += delta;
        }
        if (skeleton.getComponent(EnemyAIComponent.class).state == EnemyAIComponent.State.DEAD
            && animation.death.isAnimationFinished(animation.stateTime)) {
            PhysicsComponent physics = skeleton.getComponent(PhysicsComponent.class);
            if (physics.body.getWorld() != null) world.destroyBody(physics.body);
            enemies.removeValue(skeleton, true);
            engine.removeEntity(skeleton);
        }
    }

    private static void setVisual(AnimationComponent animation,
        AnimationComponent.State state, float time) {
        if (animation.state != state) animation.state = state;
        animation.stateTime = time;
    }
}
