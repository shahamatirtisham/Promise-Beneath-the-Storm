package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantOfferType;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** Purchases merchant stock with number keys while the player is nearby. */
public class MerchantSystem extends IteratingSystem {
    private static final float INTERACTION_RANGE_SQUARED = 1.4f * 1.4f;

    private final Entity player;

    public MerchantSystem(Entity player) {
        super(Family.all(PositionComponent.class, MerchantComponent.class).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MerchantComponent merchant = entity.getComponent(MerchantComponent.class);
        int offerIndex = selectedOfferIndex();
        if (offerIndex < 0 || offerIndex >= merchant.offerTypes.length) {
            return;
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent merchantPosition = entity.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - merchantPosition.x;
        float deltaY = playerPosition.y - merchantPosition.y;
        if (deltaX * deltaX + deltaY * deltaY > INTERACTION_RANGE_SQUARED) {
            return;
        }

        MerchantOfferType type = merchant.offerTypes[offerIndex];
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        if (type != MerchantOfferType.KNIFE && merchant.isPurchased(offerIndex)) {
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

    private int selectedOfferIndex() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            return 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            return 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            return 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
            return 3;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) {
            return 4;
        }
        return -1;
    }
}
