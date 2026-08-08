package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;

public class InputSystem extends IteratingSystem {

    private static final Family family = Family.all(
        PlayerComponent.class,
        VelocityComponent.class,
        FacingComponent.class
    ).get();

    public InputSystem() {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        VelocityComponent velocity =
            entity.getComponent(VelocityComponent.class);

        FacingComponent facing =
            entity.getComponent(FacingComponent.class);

        PlayerComponent player =
            entity.getComponent(PlayerComponent.class);

        StatusEffectComponent status =
            entity.getComponent(StatusEffectComponent.class);

        velocity.vx = 0;
        velocity.vy = 0;

        if (player.dead || player.controlsLocked || status.isStunned()) {
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.vy += 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.vy -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.vx -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.vx += 1;
        }

        // Remember the last facing direction.
        if (velocity.vx != 0 || velocity.vy != 0) {

            facing.x = velocity.vx;
            facing.y = velocity.vy;

            if (Math.abs(velocity.vx) > Math.abs(velocity.vy)) {
                facing.direction = velocity.vx > 0
                    ? FacingComponent.Direction.RIGHT
                    : FacingComponent.Direction.LEFT;
            } else {
                facing.direction = velocity.vy > 0
                    ? FacingComponent.Direction.UP
                    : FacingComponent.Direction.DOWN;
            }
        }

        float lengthSquared =
            velocity.vx * velocity.vx +
                velocity.vy * velocity.vy;

        if (lengthSquared > 0) {

            float movementSpeed =
                Constants.PLAYER_SPEED *
                    (status.isSlowed() ? 0.55f : 1f);

            float scale =
                movementSpeed /
                    (float) Math.sqrt(lengthSquared);

            velocity.vx *= scale;
            velocity.vy *= scale;
        }
    }
}
