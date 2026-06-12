package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InputSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.MovementSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;

public class GameScreen implements Screen {
    private Engine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Entity player;

    public GameScreen() {
        engine = new Engine();
        camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        viewport = new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        // Add systems in correct order
        engine.addSystem(new InputSystem());
        engine.addSystem(new MovementSystem());

        // Create player
        player = new Entity();
        player.add(new PlayerComponent());
        player.add(new PositionComponent(0, 0));
        player.add(new VelocityComponent());
        // hehe
        engine.addEntity(player);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        engine.update(delta);

        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        camera.position.set(playerPos.x, playerPos.y, 0);
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw a RED FIXED REFERENCE POINT at world origin (0,0)
        shapeRenderer.setColor(1, 0, 0, 1);  // Red
        shapeRenderer.rect(-0.2f, -0.2f, 0.4f, 0.4f);  // Fixed at (0,0)

        // Draw player as a GREEN square
        shapeRenderer.setColor(0, 1, 0, 1);  // Green
        shapeRenderer.rect(playerPos.x - 0.4f, playerPos.y - 0.4f, 0.8f, 0.8f);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    // Required but empty methods
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
