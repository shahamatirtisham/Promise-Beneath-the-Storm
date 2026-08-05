package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** Purchases a permanent-for-run health upgrade when the player presses E nearby. */
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
        if (merchant.purchased || !GamePreferences.isJustPressed(GamePreferences.Action.INTERACT)) {
            return;
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent merchantPosition = entity.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - merchantPosition.x;
        float deltaY = playerPosition.y - merchantPosition.y;
        if (deltaX * deltaX + deltaY * deltaY > INTERACTION_RANGE_SQUARED) {
            return;
        }

        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        if (inventory.devilCoins < merchant.cost) {
            Gdx.app.log(
                "Merchant",
                "Not enough Devil Coins. Need " + merchant.cost
                    + ", have " + inventory.devilCoins
            );
            return;
        }

        inventory.devilCoins -= merchant.cost;
        merchant.relicType.apply(player);
        merchant.purchased = true;

        Gdx.app.log(
            "Merchant",
            merchant.relicType.displayName + " purchased. Devil Coins remaining: "
                + inventory.devilCoins
        );
    }
}
