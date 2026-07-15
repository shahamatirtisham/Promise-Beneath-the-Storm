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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InputSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AimSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAISystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PhysicsSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

public class GameScreen implements Screen {
    private Engine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Entity player;
    private Entity enemy;

    // Box2D
    private World world;
    private Body playerBody;
    private Box2DDebugRenderer debugRenderer;

    // Room boundaries
    private static final float ROOM_LEFT = -8f;
    private static final float ROOM_RIGHT = 8f;
    private static final float ROOM_BOTTOM = -5f;
    private static final float ROOM_TOP = 5f;
    private static final float AIM_INDICATOR_DISTANCE = 1.1f;
    private static final float AIM_INDICATOR_RADIUS = 0.12f;

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

        playerBody = createDynamicCircleBody(0f, 0f, 0.4f);
        player = new Entity();
        player.add(new PlayerComponent());
        player.add(new PositionComponent(0, 0));
        player.add(new VelocityComponent());
        player.add(new PhysicsComponent(playerBody));
        player.add(new FacingComponent());
        player.add(new HealthComponent(100f));
        player.add(new TeamComponent(TeamComponent.Team.PLAYER));
        player.add(new AttackComponent());
        engine.addEntity(player);

        Body enemyBody = createDynamicCircleBody(4f, 0f, 0.45f);
        enemy = new Entity();
        enemy.add(new EnemyComponent());
        enemy.add(new EnemyAIComponent());
        enemy.add(new PositionComponent(4f, 0f));
        enemy.add(new VelocityComponent());
        enemy.add(new PhysicsComponent(enemyBody));
        enemy.add(new HealthComponent(50f));
        enemy.add(new TeamComponent(TeamComponent.Team.ENEMY));
        engine.addEntity(enemy);

        // AI and input choose velocities before the physics system applies them.
        engine.addSystem(new InputSystem());
        engine.addSystem(new EnemyAISystem(player));
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AimSystem(viewport));
        engine.addSystem(new AttackSystem());
    }

    private Body createDynamicCircleBody(float x, float y, float radius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        body.createFixture(fixtureDef);
        circle.dispose();
        return body;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        // Input runs first; physics then applies velocity and synchronizes position.
        engine.update(delta);

        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        FacingComponent playerFacing = player.getComponent(FacingComponent.class);
        AttackComponent playerAttack = player.getComponent(AttackComponent.class);
        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        EnemyAIComponent enemyAI = enemy.getComponent(EnemyAIComponent.class);

        // Camera follows player
        camera.position.set(playerPos.x, playerPos.y, 0);
        camera.update();

        float aimX = playerPos.x + playerFacing.x * AIM_INDICATOR_DISTANCE;
        float aimY = playerPos.y + playerFacing.y * AIM_INDICATOR_DISTANCE;

        // Draw filled placeholder graphics.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw player (green)
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(playerPos.x, playerPos.y, 0.4f);

        // Cyan dot shows the world-space direction derived from the mouse.
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.circle(aimX, aimY, AIM_INDICATOR_RADIUS);

        if (playerAttack.isActive()) {
            drawAttackArea(playerPos, playerFacing, playerAttack);
        }

        drawEnemy(enemyPosition, enemyAI);

        shapeRenderer.end();

        // Draw debug outlines separately so the room is not filled in.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.line(playerPos.x, playerPos.y, aimX, aimY);

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(ROOM_LEFT, ROOM_BOTTOM, ROOM_RIGHT - ROOM_LEFT, ROOM_TOP - ROOM_BOTTOM);

        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.circle(enemyPosition.x, enemyPosition.y, enemyAI.detectionRange, 48);

        shapeRenderer.end();

        // Draw Box2D debug (shows collision shapes)
        debugRenderer.render(world, camera.combined);
    }

    private void drawEnemy(PositionComponent position, EnemyAIComponent ai) {
        switch (ai.state) {
            case IDLE:
                shapeRenderer.setColor(0.45f, 0.45f, 0.45f, 1f);
                break;
            case CHASE:
                shapeRenderer.setColor(0.85f, 0.15f, 0.15f, 1f);
                break;
            case ATTACK:
                shapeRenderer.setColor(1f, 0.55f, 0.05f, 1f);
                break;
            case RECOVER:
                shapeRenderer.setColor(0.55f, 0.1f, 0.1f, 1f);
                break;
            case DEAD:
                shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
                break;
        }
        shapeRenderer.circle(position.x, position.y, 0.45f);
    }

    private void drawAttackArea(
        PositionComponent position,
        FacingComponent facing,
        AttackComponent attack
    ) {
        float startX = position.x + facing.x * 0.35f;
        float startY = position.y + facing.y * 0.35f;
        float endX = position.x + facing.x * attack.reach;
        float endY = position.y + facing.y * attack.reach;
        float perpendicularX = -facing.y * attack.halfWidth;
        float perpendicularY = facing.x * attack.halfWidth;

        float leftX = endX + perpendicularX;
        float leftY = endY + perpendicularY;
        float rightX = endX - perpendicularX;
        float rightY = endY - perpendicularY;

        shapeRenderer.setColor(1f, 0.8f, 0.1f, 1f);
        shapeRenderer.triangle(startX, startY, leftX, leftY, rightX, rightY);
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
