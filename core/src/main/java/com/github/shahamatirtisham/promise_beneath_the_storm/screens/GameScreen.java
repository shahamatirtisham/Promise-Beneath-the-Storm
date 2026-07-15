package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InputSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PhysicsSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

public class GameScreen implements Screen {
    private Engine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Entity player;

    // Box2D
    private World world;
    private Body playerBody;
    private Box2DDebugRenderer debugRenderer;

    // Room boundaries
    private static final float ROOM_LEFT = -8f;
    private static final float ROOM_RIGHT = 8f;
    private static final float ROOM_BOTTOM = -5f;
    private static final float ROOM_TOP = 5f;

    public GameScreen() {
        engine = new Engine();
        camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        viewport = new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        // Initialize Box2D world (no gravity for top-down)
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        // Create walls
        WorldUtils.createWalls(world, ROOM_LEFT, ROOM_RIGHT, ROOM_BOTTOM, ROOM_TOP);

        // Create player physics body
        createPlayerBody();

        // Add systems
        engine.addSystem(new InputSystem());
        engine.addSystem(new PhysicsSystem(world));

        // Create player entity (for ECS)
        player = new Entity();
        player.add(new PlayerComponent());
        player.add(new PositionComponent(0, 0));
        player.add(new VelocityComponent());
        player.add(new PhysicsComponent(playerBody));
        engine.addEntity(player);
    }

    private void createPlayerBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(0, 0);
        bodyDef.fixedRotation = true;

        playerBody = world.createBody(bodyDef);

        // Circle shape for player
        CircleShape circle = new CircleShape();
        circle.setRadius(0.4f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        playerBody.createFixture(fixtureDef);
        circle.dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        // Input runs first; physics then applies velocity and synchronizes position.
        engine.update(delta);

        PositionComponent playerPos = player.getComponent(PositionComponent.class);

        // Camera follows player
        camera.position.set(playerPos.x, playerPos.y, 0);
        camera.update();

        // Draw graphics
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw player (green)
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(playerPos.x, playerPos.y, 0.4f);

        // Draw walls (red outlines)
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(ROOM_LEFT, ROOM_BOTTOM, ROOM_RIGHT - ROOM_LEFT, ROOM_TOP - ROOM_BOTTOM);

        shapeRenderer.end();

        // Draw Box2D debug (shows collision shapes)
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        debugRenderer.dispose();
        world.dispose();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
