package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantOfferType;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;
import java.util.Random;

/** Creates seeded merchant stock: knives, a pouch, and three relics. */
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
        MerchantOfferType[] offerTypes = {
            MerchantOfferType.KNIFE,
            MerchantOfferType.KNIFE_POUCH,
            MerchantOfferType.RELIC,
            MerchantOfferType.RELIC,
            MerchantOfferType.RELIC
        };
        RelicType[] relicOffers = {null, null, offers[0], offers[1], offers[2]};
        int[] costs = new int[offerTypes.length];
        costs[0] = 2 + level;
        costs[1] = 8 + level * 2;
        for (int index = 0; index < offers.length; index++) {
            int typePremium = offers[index] == RelicType.STORM_EDGE ? 4
                : offers[index] == RelicType.WINDSTEP_SIGIL ? 3 : 2;
            costs[index + 2] = 6 + level * 2 + typePremium;
        }
        Entity merchant = new Entity();
        merchant.add(new PositionComponent(position.x, position.y));
        merchant.add(new MerchantComponent(offerTypes, relicOffers, costs));
        return merchant;
    }
}
