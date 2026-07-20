package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.LeverComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

public final class LeverFactory {
    private LeverFactory() {
    }

    public static Entity create(Vector2 position) {
        Entity lever = new Entity();
        lever.add(new PositionComponent(position.x, position.y));
        lever.add(new LeverComponent());
        return lever;
    }
}
