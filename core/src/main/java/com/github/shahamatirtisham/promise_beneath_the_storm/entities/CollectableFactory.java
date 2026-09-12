package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PickupAnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;

/** Creates lightweight pickups that do not require Box2D bodies. */
public final class CollectableFactory {
    private CollectableFactory() {
    }

    public static Entity createDevilCoins(Vector2 position, int value) {
        return createPickup(position, new CollectableComponent(
            CollectableComponent.Type.DEVIL_COINS,
            value
        ));
    }

    public static Entity createHeal(Vector2 position, int value) {
        return createPickup(position, new CollectableComponent(
            CollectableComponent.Type.HEAL,
            value
        ));
    }

    public static Entity createReward(
        Vector2 position,
        CollectableComponent.Type type,
        int value
    ) {
        return createReward(position, type, value, null);
    }

    public static Entity createReward(
        Vector2 position,
        CollectableComponent.Type type,
        int value,
        RelicType relicType
    ) {
        Entity collectable = new Entity();
        collectable.add(new PositionComponent(position.x, position.y));
        collectable.add(new CollectableComponent(type, value, relicType, true, false));
        if (usesFloatingAnimation(type)) {
            collectable.add(new PickupAnimationComponent());
        }
        return collectable;
    }

    public static Entity createKnife(Vector2 position, boolean bonusKnife) {
        Entity collectable = new Entity();
        collectable.add(new PositionComponent(position.x, position.y));
        collectable.add(new CollectableComponent(
            CollectableComponent.Type.KNIFE,
            1,
            null,
            false,
            bonusKnife
        ));
        return collectable;
    }

    private static Entity createPickup(
        Vector2 position,
        CollectableComponent collectableData
    ) {
        Entity collectable = new Entity();
        collectable.add(new PositionComponent(position.x, position.y));
        collectable.add(collectableData);
        collectable.add(new PickupAnimationComponent());
        return collectable;
    }

    private static boolean usesFloatingAnimation(CollectableComponent.Type type) {
        return type == CollectableComponent.Type.DEVIL_COINS
            || type == CollectableComponent.Type.HEAL;
    }
}
