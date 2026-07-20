package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;

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

        if (collectable.type == CollectableComponent.Type.HEAL) {
            HealthComponent health = player.getComponent(HealthComponent.class);
            if (health.current >= health.maximum) {
                return;
            }
        }

        applyReward(collectable);
        collectable.collected = true;
    }

    private void applyReward(CollectableComponent collectable) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        switch (collectable.type) {
            case DEVIL_COINS:
                RunInventoryComponent inventory =
                    player.getComponent(RunInventoryComponent.class);
                inventory.devilCoins += collectable.value;
                Gdx.app.log("Loot", "+" + collectable.value + " Devil Coins");
                break;
            case HEAL:
                float previousHealth = health.current;
                health.current = Math.min(health.maximum, health.current + collectable.value);
                Gdx.app.log(
                    "Loot",
                    "+" + Math.round(health.current - previousHealth) + " Health"
                );
                break;
            case MAX_HEALTH:
                health.maximum += collectable.value;
                health.current = Math.min(health.maximum, health.current + collectable.value);
                Gdx.app.log("Loot", "+" + collectable.value + " Maximum Health");
                break;
            case RELIC:
                collectable.relicType.apply(player);
                break;
        }
    }
}
