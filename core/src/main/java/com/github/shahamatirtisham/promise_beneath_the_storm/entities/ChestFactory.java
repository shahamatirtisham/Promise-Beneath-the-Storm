package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;

public final class ChestFactory {
    private ChestFactory() {
    }

    public static Entity create(
        Vector2 position,
        CollectableComponent.Type rewardType,
        int rewardValue,
        RelicType relicType
    ) {
        Entity chest = new Entity();
        chest.add(new PositionComponent(position.x, position.y));
        chest.add(new ChestComponent(rewardType, rewardValue, relicType));
        return chest;
    }
}
