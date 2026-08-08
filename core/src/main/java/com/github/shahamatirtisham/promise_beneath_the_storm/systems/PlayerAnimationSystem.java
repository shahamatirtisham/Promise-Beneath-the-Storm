package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;

public class PlayerAnimationSystem extends IteratingSystem {

    public PlayerAnimationSystem() {
        super(Family.all(
            PhysicsComponent.class,
            PlayerAnimationComponent.class,
            AttackComponent.class
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

        Vector2 velocity = physics.body.getLinearVelocity();

        float speed = velocity.len();

        // Decide current animation state
        if (attack.isActive()) {

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
}
