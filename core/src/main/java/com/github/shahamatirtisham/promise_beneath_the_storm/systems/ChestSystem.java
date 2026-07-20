package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.CollectableFactory;

/** Opens nearby unlocked chests and releases their reward pickup. */
public class ChestSystem extends IteratingSystem {
    private static final float INTERACTION_RANGE_SQUARED = 1.1f * 1.1f;
    private final Engine engine;
    private final Entity player;
    private final Array<Entity> collectables;

    public ChestSystem(Engine engine, Entity player, Array<Entity> collectables) {
        super(Family.all(PositionComponent.class, ChestComponent.class).get());
        this.engine = engine;
        this.player = player;
        this.collectables = collectables;
    }

    @Override
    protected void processEntity(Entity chest, float deltaTime) {
        ChestComponent data = chest.getComponent(ChestComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (data.opened || !data.unlocked || playerState.dead
            || playerState.controlsLocked
            || !Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            return;
        }

        PositionComponent chestPosition = chest.getComponent(PositionComponent.class);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - chestPosition.x;
        float deltaY = playerPosition.y - chestPosition.y;
        if (deltaX * deltaX + deltaY * deltaY > INTERACTION_RANGE_SQUARED) {
            return;
        }

        data.opened = true;
        Entity reward = CollectableFactory.createDevilCoins(
            new Vector2(chestPosition.x, chestPosition.y),
            data.coinValue
        );
        collectables.add(reward);
        engine.addEntity(reward);
        Gdx.app.log("Chest", "Chest opened - reward released");
    }
}
