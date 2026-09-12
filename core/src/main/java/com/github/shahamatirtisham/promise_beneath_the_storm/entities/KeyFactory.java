package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestKeyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PickupAnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

public final class KeyFactory {
    private KeyFactory() {
    }

    public static Entity create(Vector2 position) {
        Entity key = new Entity();
        key.add(new PositionComponent(position.x, position.y));
        key.add(new ChestKeyComponent());
        key.add(new PickupAnimationComponent());
        return key;
    }
}
