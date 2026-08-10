package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;

public class PlayerAnimationSystem extends IteratingSystem {

    public PlayerAnimationSystem() {
        super(Family.all(
            PhysicsComponent.class,
            PlayerAnimationComponent.class,
            AttackComponent.class,
            DefenseComponent.class,
            PlayerComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        PlayerAnimationComponent animation =
            entity.getComponent(PlayerAnimationComponent.class);

        PhysicsComponent physics =
            entity.getComponent(PhysicsComponent.class);

        AttackComponent attack =
            entity.getComponent(AttackComponent.class);

        PlayerComponent player =
            entity.getComponent(PlayerComponent.class);

        DefenseComponent defense =
            entity.getComponent(DefenseComponent.class);

        Vector2 velocity = physics.body.getLinearVelocity();

        float speed = velocity.len();

        // Death has priority and holds its final frame until the run restarts.
        if (player.dead) {

            setState(animation, PlayerAnimationComponent.State.DEAD);

        } else if (animation.parryAnimationRequested) {

            animation.parryAnimationRequested = false;
            restartState(animation, PlayerAnimationComponent.State.PARRY);

        } else if (animation.state == PlayerAnimationComponent.State.PARRY
            && !isParryAnimationFinished(animation)) {

            // A successful parry owns the sprite until its recovery frame ends.

        } else if (defense.blocking) {

            setState(animation, PlayerAnimationComponent.State.BLOCK);

        } else if (attack.isActive()) {

            setState(animation, PlayerAnimationComponent.State.ATTACK);

        } else if (speed < 0.05f) {

            setState(animation, PlayerAnimationComponent.State.IDLE);

        } else {

            setState(animation, PlayerAnimationComponent.State.WALK);
        }

        // Update facing direction
        if (velocity.x < 0f) {

            animation.facingLeft = true;

        } else if (velocity.x > 0f) {

            animation.facingLeft = false;
        }

        animation.stateTime += deltaTime;
    }

    private void setState(
        PlayerAnimationComponent animation,
        PlayerAnimationComponent.State state) {

        if (animation.state != state) {

            animation.state = state;
            animation.stateTime = 0f;
        }
    }

    private void restartState(
        PlayerAnimationComponent animation,
        PlayerAnimationComponent.State state
    ) {
        animation.state = state;
        animation.stateTime = 0f;
    }

    private boolean isParryAnimationFinished(
        PlayerAnimationComponent animation
    ) {
        switch (animation.parryDirection) {
            case UP:
                return animation.parryUp.isAnimationFinished(animation.stateTime);
            case LEFT:
                return animation.parryLeft.isAnimationFinished(animation.stateTime);
            case RIGHT:
                return animation.parryRight.isAnimationFinished(animation.stateTime);
            case DOWN:
            default:
                return animation.parryDown.isAnimationFinished(animation.stateTime);
        }
    }
}
