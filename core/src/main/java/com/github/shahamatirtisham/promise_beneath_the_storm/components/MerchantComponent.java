package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;

/** Seeded merchant stock. Consumable knives and bombs can be bought repeatedly. */
public class MerchantComponent implements Component {
    public final int[] costs;
    public final MerchantOfferType[] offerTypes;
    public final RelicType[] relicOffers;
    public int purchasedMask;
    public Rectangle interactionBounds;

    public boolean canInteract(float playerX, float playerY, float merchantX, float merchantY) {
        if (interactionBounds != null) {
            return interactionBounds.contains(playerX, playerY);
        }
        float deltaX = playerX - merchantX;
        float deltaY = playerY - merchantY;
        return deltaX * deltaX + deltaY * deltaY <= 1.4f * 1.4f;
    }

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
