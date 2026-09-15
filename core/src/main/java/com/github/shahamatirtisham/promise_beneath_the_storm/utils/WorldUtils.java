package com.github.shahamatirtisham.promise_beneath_the_storm.utils;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.math.Rectangle;

public class WorldUtils {
    public static final short WITCH_COLLISION_CATEGORY = 0x0002;
    private static final short WITCH_BOUNDARY_COLLISION_CATEGORY = 0x0004;
    private static final float ENEMY_BOUNDARY_THICKNESS = 1f;

    public static void configureWitchBody(Body body) {
        body.setBullet(true);
        for (Fixture fixture : body.getFixtureList()) {
            Filter filter = fixture.getFilterData();
            filter.categoryBits |= WITCH_COLLISION_CATEGORY;
            fixture.setFilterData(filter);
        }
    }

    /** Seal the map's outer edge for witches, including open doorways.
     * Player/default fixtures retain doorway traversal and all authored walls.
     */
    public static com.badlogic.gdx.utils.Array<Body> createWitchRoomBounds(
        World world, float width, float height) {
        float thickness = ENEMY_BOUNDARY_THICKNESS;
        com.badlogic.gdx.utils.Array<Body> walls = new com.badlogic.gdx.utils.Array<>();
        walls.add(createWall(world, -thickness / 2f, height / 2f,
            thickness, height + 2f * thickness));
        walls.add(createWall(world, width + thickness / 2f, height / 2f,
            thickness, height + 2f * thickness));
        walls.add(createWall(world, width / 2f, -thickness / 2f,
            width + 2f * thickness, thickness));
        walls.add(createWall(world, width / 2f, height + thickness / 2f,
            width + 2f * thickness, thickness));
        for (Body wall : walls) {
            Filter filter = wall.getFixtureList().first().getFilterData();
            filter.categoryBits = WITCH_BOUNDARY_COLLISION_CATEGORY;
            filter.maskBits = WITCH_COLLISION_CATEGORY;
            wall.getFixtureList().first().setFilterData(filter);
        }
        return walls;
    }

    public static void createWalls(World world, float left, float right, float bottom, float top) {
        createWall(world, left, (bottom + top) / 2f, 0.1f, top - bottom);
        createWall(world, right, (bottom + top) / 2f, 0.1f, top - bottom);
        createWall(world, (left + right) / 2f, bottom, right - left, 0.1f);
        createWall(world, (left + right) / 2f, top, right - left, 0.1f);
    }

    /** Creates solid edges along the authored outline, including any closing segment. */
    public static Body createStaticPolyline(World world, float[] vertices) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        Body wall = world.createBody(bodyDef);
        EdgeShape edge = new EdgeShape();
        try {
            for (int index = 0; index + 3 < vertices.length; index += 2) {
                edge.set(vertices[index], vertices[index + 1],
                    vertices[index + 2], vertices[index + 3]);
                wall.createFixture(edge, 0f);
            }
        } finally {
            edge.dispose();
        }
        return wall;
    }

    public static Body createStaticRectangle(World world, Rectangle rectangle) {
        return createWall(
            world,
            rectangle.x + rectangle.width / 2f,
            rectangle.y + rectangle.height / 2f,
            rectangle.width,
            rectangle.height
        );
    }

    public static Body createDynamicCircle(
        World world,
        float x,
        float y,
        float radius
    ) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef fixtureDefinition = new FixtureDef();
        fixtureDefinition.shape = circle;
        fixtureDefinition.density = 1f;
        fixtureDefinition.friction = 0f;
        fixtureDefinition.restitution = 0f;
        body.createFixture(fixtureDefinition);
        circle.dispose();
        return body;
    }

    private static Body createWall(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body wall = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        wall.createFixture(shape, 0);
        shape.dispose();
        return wall;
    }
}
