package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;
import java.util.Random;

/** Creates the placeholder health-upgrade merchant. */
public final class MerchantFactory {
    private MerchantFactory() {
    }

    public static Entity createRelicMerchant(
        Vector2 position,
        int level,
        long seed
    ) {
        RelicType[] offers = RelicType.values().clone();
        Random random = new Random(seed);
        for (int index = offers.length - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            RelicType swap = offers[index];
            offers[index] = offers[swapIndex];
            offers[swapIndex] = swap;
        }
        int[] costs = new int[offers.length];
        for (int index = 0; index < offers.length; index++) {
            int typePremium = offers[index] == RelicType.STORM_EDGE ? 4
                : offers[index] == RelicType.WINDSTEP_SIGIL ? 3 : 2;
            costs[index] = 6 + level * 2 + typePremium;
        }
        Entity merchant = new Entity();
        merchant.add(new PositionComponent(position.x, position.y));
        merchant.add(new MerchantComponent(offers, costs));
        return merchant;
    }
}
