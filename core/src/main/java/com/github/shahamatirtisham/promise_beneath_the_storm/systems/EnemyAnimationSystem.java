package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.AnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;

public class EnemyAnimationSystem extends IteratingSystem {

    public EnemyAnimationSystem() {

        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            AnimationComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {

        EnemyAIComponent ai =
            enemy.getComponent(EnemyAIComponent.class);

        AnimationComponent animation =
            enemy.getComponent(AnimationComponent.class);

        /*
         * Convert the existing gameplay AI state
         * into the corresponding visual animation.
         */
        switch (ai.state) {

            case IDLE:
                setAnimationState(
                    animation,
                    AnimationComponent.State.IDLE
                );
                break;

            case CHASE:
                setAnimationState(
                    animation,
                    AnimationComponent.State.WALK
                );
                break;

            case ATTACK:
                setAnimationState(
                    animation,
                    AnimationComponent.State.ATTACK
                );
                break;

            case RECOVER:
                setAnimationState(
                    animation,
                    AnimationComponent.State.IDLE
                );
                break;

            case STUNNED:
                setAnimationState(
                    animation,
                    AnimationComponent.State.HURT
                );
                break;

            case DEAD:
                setAnimationState(
                    animation,
                    AnimationComponent.State.DEAD
                );
                break;
        }

        /*
         * Advance the current animation.
         */
        animation.stateTime += deltaTime;
    }

    private void setAnimationState(
        AnimationComponent animation,
        AnimationComponent.State newState
    ) {

        /*
         * Don't reset the animation every frame.
         */
        if (animation.state != newState) {

            animation.state = newState;

            /*
             * Start the new animation from frame 0.
             */
            animation.stateTime = 0f;
        }
    }
}
