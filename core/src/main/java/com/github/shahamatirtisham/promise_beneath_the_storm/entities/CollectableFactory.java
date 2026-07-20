package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Creates lightweight pickups that do not require Box2D bodies. */
public final class CollectableFactory {
    private CollectableFactory() {
    }

    public static Entity createDevilCoins(Vector2 position, int value) {
        Entity collectable = new Entity();
        collectable.add(new PositionComponent(position.x, position.y));
        collectable.add(new CollectableComponent(
            CollectableComponent.Type.DEVIL_COINS,
            value
        ));
        return collectable;
    }

    public static Entity createReward(
        Vector2 position,
        CollectableComponent.Type type,
        int value
    ) {
        Entity collectable = new Entity();
        collectable.add(new PositionComponent(position.x, position.y));
        collectable.add(new CollectableComponent(type, value));
        return collectable;
    }
}
