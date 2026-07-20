package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestKeyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Collects a room key when the player walks close to it. */
public class KeyCollectionSystem extends IteratingSystem {
    private static final float COLLECTION_RANGE_SQUARED = 0.75f * 0.75f;
    private final Entity player;

    public KeyCollectionSystem(Entity player) {
        super(Family.all(PositionComponent.class, ChestKeyComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity key, float deltaTime) {
        ChestKeyComponent data = key.getComponent(ChestKeyComponent.class);
        if (data.collected) {
            return;
        }
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent keyPosition = key.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - keyPosition.x;
        float deltaY = playerPosition.y - keyPosition.y;
        if (deltaX * deltaX + deltaY * deltaY <= COLLECTION_RANGE_SQUARED) {
            data.collected = true;
            Gdx.app.log("Key", "Chest key collected");
        }
    }
}
