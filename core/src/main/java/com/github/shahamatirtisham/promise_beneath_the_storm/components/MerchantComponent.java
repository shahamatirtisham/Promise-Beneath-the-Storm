package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Seeded merchant stock. Consumable knives can be bought repeatedly. */
public class MerchantComponent implements Component {
    public final int[] costs;
    public final MerchantOfferType[] offerTypes;
    public final RelicType[] relicOffers;
    public int purchasedMask;

    public MerchantComponent(
        MerchantOfferType[] offerTypes,
        RelicType[] relicOffers,
        int[] costs
    ) {
        this.offerTypes = offerTypes;
        this.relicOffers = relicOffers;
        this.costs = costs;
    }

    public boolean isPurchased(int index) {
        return (purchasedMask & (1 << index)) != 0;
    }

    public void markPurchased(int index) {
        purchasedMask |= 1 << index;
    }
}
