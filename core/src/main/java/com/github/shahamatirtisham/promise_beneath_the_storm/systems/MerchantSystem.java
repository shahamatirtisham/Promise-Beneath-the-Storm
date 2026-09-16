package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantOfferType;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;

/** Validates and delivers purchases requested by the merchant popup. */
public class MerchantSystem extends IteratingSystem {
    private final Entity player;

    public MerchantSystem(Entity player) {
        super(Family.all(PositionComponent.class, MerchantComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Purchases are initiated by the merchant popup buttons.
    }

    /** Null means the offer is available; the UI displays other results as a reason. */
    public String getUnavailableReason(Entity merchantEntity, int offerIndex) {
        if (merchantEntity == null) return "Merchant unavailable";
        MerchantComponent merchant = merchantEntity.getComponent(MerchantComponent.class);
        if (merchant == null || offerIndex < 0 || offerIndex >= merchant.offerTypes.length)
            return "Offer unavailable";
        PlayerComponent state = player.getComponent(PlayerComponent.class);
        if (state == null || state.dead) return "Cannot purchase while defeated";
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent merchantPosition = merchantEntity.getComponent(PositionComponent.class);
        if (playerPosition == null || merchantPosition == null || !merchant.canInteract(
            playerPosition.x, playerPosition.y, merchantPosition.x, merchantPosition.y
        )) {
            return "Move closer to the merchant";
        }

        MerchantOfferType type = merchant.offerTypes[offerIndex];
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        if (type != MerchantOfferType.KNIFE && type != MerchantOfferType.BOMB
            && merchant.isPurchased(offerIndex)) {
            return "Sold out";
        }
        if (type == MerchantOfferType.KNIFE && knives.charges >= knives.maximumCharges) {
            return "Knife pouch is full";
        }
        if (type == MerchantOfferType.KNIFE_POUCH
            && knives.maximumCharges >= PlayerRangedComponent.MAXIMUM_POUCH_CAPACITY) {
            return "Pouch is at maximum capacity";
        }

        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        int cost = merchant.costs[offerIndex];
        if (inventory.devilCoins < cost) {
            return "Need " + (cost - inventory.devilCoins) + " more coins";
        }
        return null;
    }

    public String getInventorySummary() {
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        return "Coins: " + player.getComponent(RunInventoryComponent.class).devilCoins
            + "    Knives: " + knives.charges + "/" + knives.maximumCharges
            + "    Bombs: " + player.getComponent(PlayerComponent.class).bombCharges;
    }

    public String purchaseOffer(Entity entity, int offerIndex) {
        String unavailable = getUnavailableReason(entity, offerIndex);
        if (unavailable != null) return unavailable;
        MerchantComponent merchant = entity.getComponent(MerchantComponent.class);
        MerchantOfferType type = merchant.offerTypes[offerIndex];
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        int cost = merchant.costs[offerIndex];

        inventory.devilCoins -= cost;
        String purchasedName;
        if (type == MerchantOfferType.KNIFE) {
            knives.addKnife();
            purchasedName = "Knife";
        } else if (type == MerchantOfferType.BOMB) {
            player.getComponent(PlayerComponent.class).bombCharges++;
            purchasedName = "Bomb";
        } else if (type == MerchantOfferType.KNIFE_POUCH) {
            knives.upgradePouch();
            merchant.markPurchased(offerIndex);
            purchasedName = "Knife Pouch";
        } else {
            merchant.relicOffers[offerIndex].apply(player);
            merchant.markPurchased(offerIndex);
            purchasedName = merchant.relicOffers[offerIndex].displayName;
        }

        Gdx.app.log(
            "Merchant",
            purchasedName
                + " purchased. Devil Coins remaining: "
                + inventory.devilCoins
        );
        return purchasedName + " purchased for " + cost + " coins";
    }

}
