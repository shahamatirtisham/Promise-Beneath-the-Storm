package com.github.shahamatirtisham.promise_beneath_the_storm.utils;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.math.Rectangle;

public class WorldUtils {

    public static void createWalls(World world, float left, float right, float bottom, float top) {
        createWall(world, left, (bottom + top) / 2f, 0.1f, top - bottom);
        createWall(world, right, (bottom + top) / 2f, 0.1f, top - bottom);
        createWall(world, (left + right) / 2f, bottom, right - left, 0.1f);
        createWall(world, (left + right) / 2f, top, right - left, 0.1f);
    }

    public static void createStaticRectangle(World world, Rectangle rectangle) {
        createWall(
            world,
            rectangle.x + rectangle.width / 2f,
            rectangle.y + rectangle.height / 2f,
            rectangle.width,
            rectangle.height
        );
    }

    private static void createWall(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body wall = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        wall.createFixture(shape, 0);
        shape.dispose();
    }
}
