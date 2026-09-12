package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BreakablePotComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

public final class BreakablePotFactory {
    public static final float HIT_RADIUS = 0.38f;
    public static final float BODY_WIDTH = 0.56f;
    public static final float BODY_HEIGHT = 0.28f;
    public static final float BODY_CENTER_Y_OFFSET = -0.3f;

    private BreakablePotFactory() {
    }

    public static Entity create(
        World world,
        float x,
        float y,
        int variant,
        int roomIndex,
        int spawnIndex
    ) {
        Entity entity = new Entity();

        Body body = WorldUtils.createStaticRectangle(
            world,
            new Rectangle(
                x - BODY_WIDTH / 2f,
                y + BODY_CENTER_Y_OFFSET - BODY_HEIGHT / 2f,
                BODY_WIDTH,
                BODY_HEIGHT
            )
        );

        BreakablePotComponent pot = new BreakablePotComponent();
        pot.variant = variant;
        pot.roomIndex = roomIndex;
        pot.spawnIndex = spawnIndex;
        entity.add(new PositionComponent(x, y));
        entity.add(new PhysicsComponent(body));
        entity.add(pot);
        return entity;
    }
}
