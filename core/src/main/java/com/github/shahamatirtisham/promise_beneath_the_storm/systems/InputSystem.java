package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;

public class InputSystem extends IteratingSystem {

    private static final Family family = Family.all(
        PlayerComponent.class,
        VelocityComponent.class
    ).get();

    public InputSystem() {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        PlayerComponent player = entity.getComponent(PlayerComponent.class);

        velocity.vx = 0;
        velocity.vy = 0;

        if (player.dead || player.controlsLocked) {
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

        float lengthSquared = velocity.vx * velocity.vx + velocity.vy * velocity.vy;
        if (lengthSquared > 0) {
            float scale = Constants.PLAYER_SPEED / (float) Math.sqrt(lengthSquared);
            velocity.vx *= scale;
            velocity.vy *= scale;
        }
    }
}
