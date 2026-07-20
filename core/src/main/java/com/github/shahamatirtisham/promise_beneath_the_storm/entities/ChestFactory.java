package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

public final class ChestFactory {
    private ChestFactory() {
    }

    public static Entity create(Vector2 position, int coinValue) {
        Entity chest = new Entity();
        chest.add(new PositionComponent(position.x, position.y));
        chest.add(new ChestComponent(coinValue));
        return chest;
    }
}
