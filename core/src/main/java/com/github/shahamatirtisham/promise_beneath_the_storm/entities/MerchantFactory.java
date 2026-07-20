package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;

/** Creates the placeholder health-upgrade merchant. */
public final class MerchantFactory {
    private MerchantFactory() {
    }

    public static Entity createRelicMerchant(
        Vector2 position,
        int cost,
        RelicType relicType
    ) {
        Entity merchant = new Entity();
        merchant.add(new PositionComponent(position.x, position.y));
        merchant.add(new MerchantComponent(cost, relicType));
        return merchant;
    }
}
