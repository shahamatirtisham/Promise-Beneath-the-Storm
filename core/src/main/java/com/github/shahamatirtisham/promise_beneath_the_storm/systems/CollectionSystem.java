package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;

/** Transfers nearby coin pickups into the player's persistent run inventory. */
public class CollectionSystem extends IteratingSystem {
    private static final float COLLECTION_RANGE_SQUARED = 0.75f * 0.75f;

    private final Entity player;

    public CollectionSystem(Entity player) {
        super(Family.all(PositionComponent.class, CollectableComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollectableComponent collectable = entity.getComponent(CollectableComponent.class);
        if (collectable.collected) {
            return;
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent collectablePosition = entity.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - collectablePosition.x;
        float deltaY = playerPosition.y - collectablePosition.y;
        if (deltaX * deltaX + deltaY * deltaY > COLLECTION_RANGE_SQUARED) {
            return;
        }

        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        inventory.devilCoins += collectable.coinValue;
        collectable.collected = true;
        Gdx.app.log("Inventory", "Devil Coins: " + inventory.devilCoins);
    }
}
