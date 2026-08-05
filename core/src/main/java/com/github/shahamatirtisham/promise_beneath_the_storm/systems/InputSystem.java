package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;

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

        StatusEffectComponent status = entity.getComponent(StatusEffectComponent.class);
        if (player.dead || player.controlsLocked || status.isStunned()) {
            return;
        }

        if (GamePreferences.isPressed(Action.MOVE_UP)) {
            velocity.vy += 1;
        }
        if (GamePreferences.isPressed(Action.MOVE_DOWN)) {
            velocity.vy -= 1;
        }
        if (GamePreferences.isPressed(Action.MOVE_LEFT)) {
            velocity.vx -= 1;
        }
        if (GamePreferences.isPressed(Action.MOVE_RIGHT)) {
            velocity.vx += 1;
        }

        float lengthSquared = velocity.vx * velocity.vx + velocity.vy * velocity.vy;
        if (lengthSquared > 0) {
            float movementSpeed = Constants.PLAYER_SPEED * (status.isSlowed() ? 0.55f : 1f);
            float scale = movementSpeed / (float) Math.sqrt(lengthSquared);
            velocity.vx *= scale;
            velocity.vy *= scale;
        }
    }
}
