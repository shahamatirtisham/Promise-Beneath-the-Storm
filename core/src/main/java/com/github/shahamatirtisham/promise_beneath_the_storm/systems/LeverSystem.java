package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.LeverComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Activates a nearby lever when the player presses the interaction key. */
public class LeverSystem extends IteratingSystem {
    private static final float INTERACTION_RANGE_SQUARED = 1.1f * 1.1f;
    private final Entity player;

    public LeverSystem(Entity player) {
        super(Family.all(PositionComponent.class, LeverComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity lever, float deltaTime) {
        LeverComponent data = lever.getComponent(LeverComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (data.activated || playerState.dead || playerState.controlsLocked
            || !Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            return;
        }

        PositionComponent leverPosition = lever.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - leverPosition.x;
        float deltaY = playerPosition.y - leverPosition.y;
        if (deltaX * deltaX + deltaY * deltaY > INTERACTION_RANGE_SQUARED) {
            return;
        }

        data.activated = true;
        Gdx.app.log("Lever", "Lever activated - chest unlocked");
    }
}
