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

/** Purchases merchant stock with number keys while the player is nearby. */
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

    public void purchaseOffer(Entity merchantEntity, int offerIndex) {
        if (merchantEntity == null) return;
        MerchantComponent merchant = merchantEntity.getComponent(MerchantComponent.class);
        if (merchant == null || offerIndex < 0 || offerIndex >= merchant.offerTypes.length) return;
        purchase(merchantEntity, merchant, offerIndex);
    }

    private void purchase(Entity entity, MerchantComponent merchant, int offerIndex) {
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent merchantPosition = entity.getComponent(PositionComponent.class);
        if (!merchant.canInteract(
            playerPosition.x, playerPosition.y, merchantPosition.x, merchantPosition.y
        )) {
            return;
        }

        MerchantOfferType type = merchant.offerTypes[offerIndex];
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        if (type != MerchantOfferType.KNIFE && type != MerchantOfferType.BOMB
            && merchant.isPurchased(offerIndex)) {
            Gdx.app.log("Merchant", "That item is already sold out");
            return;
        }
        if (type == MerchantOfferType.KNIFE && knives.charges >= knives.maximumCharges) {
            Gdx.app.log("Merchant", "Knife pouch is full");
            return;
        }
        if (type == MerchantOfferType.KNIFE_POUCH
            && knives.maximumCharges >= PlayerRangedComponent.MAXIMUM_POUCH_CAPACITY) {
            Gdx.app.log("Merchant", "Knife pouch is already at maximum capacity");
            return;
        }

        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        int cost = merchant.costs[offerIndex];
        if (inventory.devilCoins < cost) {
            Gdx.app.log(
                "Merchant",
                "Not enough Devil Coins. Need " + cost
                    + ", have " + inventory.devilCoins
            );
            return;
        }

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
    }

}
