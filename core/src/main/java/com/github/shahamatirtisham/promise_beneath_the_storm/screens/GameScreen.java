package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InputSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AimSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAISystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DamageSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DeathSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InvulnerabilitySystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomLoader;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.DungeonLayout;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomAccretionGenerator;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GeneratedRoom;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GridDirection;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomTemplate;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomType;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.PlayerFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
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
    private RoomDefinition room;
    private final Array<Body> roomCollisionBodies = new Array<>();
    private static final String ROOM_TEMPLATE_A = "maps/level1/placeholder_room.tmx";
    private static final String ROOM_TEMPLATE_B = "maps/level1/placeholder_room_b.tmx";
    private boolean[] clearedRooms;
    private int currentRoomIndex;
    private DungeonLayout generatedLayout;

    // Box2D
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private static final float AIM_INDICATOR_DISTANCE = 1.1f;
    private static final float AIM_INDICATOR_RADIUS = 0.12f;

    public GameScreen() {
        engine = new Engine();
        camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        viewport = new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        generatedLayout = new RoomAccretionGenerator(
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.START),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.COMBAT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.LOOT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.MERCHANT),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.ELITE),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.EXIT)
        )
            .generate(6, System.currentTimeMillis());
        clearedRooms = new boolean[generatedLayout.rooms.size()];
        for (GeneratedRoom generatedRoom : generatedLayout.rooms) {
            clearedRooms[generatedRoom.id] = !generatedRoom.type.requiresClear;
        }
        Gdx.app.log("DungeonGenerator", "\n" + generatedLayout.toDebugString());
        logCurrentRoom();
        room = RoomLoader.load(generatedLayout.getRoom(currentRoomIndex).templatePath);

        // Initialize Box2D world (no gravity for top-down)
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        createRoomCollisionBodies();

        player = PlayerFactory.create(world, room.playerSpawn);
        engine.addEntity(player);

        enemy = EnemyFactory.createMelee(world, room.enemySpawn);
        engine.addEntity(enemy);

        // AI and input choose velocities before the physics system applies them.
        engine.addSystem(new InputSystem());
        engine.addSystem(new EnemyAISystem(player));
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AimSystem(viewport));
        engine.addSystem(new AttackSystem());
        engine.addSystem(new InvulnerabilitySystem());
        engine.addSystem(new DamageSystem(player));
        engine.addSystem(new EnemyAttackSystem(player));
        engine.addSystem(new DeathSystem());
        resetEnemyForCurrentRoom();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        // Input runs first; physics then applies velocity and synchronizes position.
        engine.update(delta);

        EnemyAIComponent enemyAI = enemy.getComponent(EnemyAIComponent.class);
        if (enemyAI.state == EnemyAIComponent.State.DEAD) {
            clearedRooms[currentRoomIndex] = true;
        }

        handleRoomTransition();

        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        FacingComponent playerFacing = player.getComponent(FacingComponent.class);
        AttackComponent playerAttack = player.getComponent(AttackComponent.class);
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent playerInvulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
        enemyAI = enemy.getComponent(EnemyAIComponent.class);
        HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
        InvulnerabilityComponent enemyInvulnerability =
            enemy.getComponent(InvulnerabilityComponent.class);

        // Camera follows player
        camera.position.set(playerPos.x, playerPos.y, 0);
        camera.update();

        float aimX = playerPos.x + playerFacing.x * AIM_INDICATOR_DISTANCE;
        float aimY = playerPos.y + playerFacing.y * AIM_INDICATOR_DISTANCE;

        // Draw filled placeholder graphics.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        setRoomBackgroundColor(generatedLayout.getRoom(currentRoomIndex).type);
        shapeRenderer.rect(0f, 0f, room.width, room.height);

        drawDoors();

        // Player flashes white after taking a hit.
        if (playerInvulnerability.isActive()) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        } else {
            shapeRenderer.setColor(0f, 1f, 0f, 1f);
        }
        shapeRenderer.circle(playerPos.x, playerPos.y, 0.4f);
        drawHealthBar(playerPos, playerHealth);

        // Cyan dot shows the world-space direction derived from the mouse.
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.circle(aimX, aimY, AIM_INDICATOR_RADIUS);

        if (playerAttack.isActive()) {
            drawAttackArea(playerPos, playerFacing, playerAttack);
        }

        if (!clearedRooms[currentRoomIndex]) {
            drawEnemy(enemyPosition, enemyAI, enemyHealth, enemyInvulnerability);
        }

        shapeRenderer.end();

        // Draw debug outlines separately so the room is not filled in.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.line(playerPos.x, playerPos.y, aimX, aimY);

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(0f, 0f, room.width, room.height);

        if (!clearedRooms[currentRoomIndex]) {
            shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
            shapeRenderer.circle(enemyPosition.x, enemyPosition.y, enemyAI.detectionRange, 48);
        }

        shapeRenderer.end();

        // Draw Box2D debug (shows collision shapes)
        debugRenderer.render(world, camera.combined);
    }

    private void createRoomCollisionBodies() {
        for (Rectangle collision : room.collisionRectangles) {
            roomCollisionBodies.add(WorldUtils.createStaticRectangle(world, collision));
        }
    }

    private void handleRoomTransition() {
        if (!clearedRooms[currentRoomIndex]) {
            return;
        }

        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        GeneratedRoom generatedRoom = generatedLayout.getRoom(currentRoomIndex);
        for (java.util.Map.Entry<GridDirection, Integer> connection
            : generatedRoom.connections.entrySet()) {
            Rectangle door = room.doors.get(connection.getKey());
            if (door.contains(playerPosition.x, playerPosition.y)) {
                loadRoom(connection.getValue(), connection.getKey().opposite());
                return;
            }
        }
    }

    private void loadRoom(int roomIndex, GridDirection arrivalDoor) {
        for (Body body : roomCollisionBodies) {
            world.destroyBody(body);
        }
        roomCollisionBodies.clear();

        currentRoomIndex = roomIndex;
        room = RoomLoader.load(generatedLayout.getRoom(currentRoomIndex).templatePath);
        logCurrentRoom();
        createRoomCollisionBodies();

        Vector2 playerSpawn = room.doorSpawns.get(arrivalDoor);
        Body playerBody = player.getComponent(PhysicsComponent.class).body;
        playerBody.setTransform(playerSpawn, 0f);
        playerBody.setLinearVelocity(0f, 0f);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        playerPosition.x = playerSpawn.x;
        playerPosition.y = playerSpawn.y;

        resetEnemyForCurrentRoom();
    }

    private void resetEnemyForCurrentRoom() {
        PhysicsComponent physics = enemy.getComponent(PhysicsComponent.class);
        HealthComponent health = enemy.getComponent(HealthComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        PositionComponent position = enemy.getComponent(PositionComponent.class);

        velocity.vx = 0f;
        velocity.vy = 0f;
        ai.attackPending = false;
        ai.stateTimeRemaining = 0f;

        if (clearedRooms[currentRoomIndex]) {
            health.current = 0f;
            ai.state = EnemyAIComponent.State.DEAD;
            physics.body.setActive(false);
            return;
        }

        configureEnemyForRoom(generatedLayout.getRoom(currentRoomIndex).type, health, ai);
        health.current = health.maximum;
        ai.state = EnemyAIComponent.State.IDLE;
        EnemyComponent enemyData = enemy.getComponent(EnemyComponent.class);
        enemyData.lastPlayerAttackId = -1;
        physics.body.setActive(true);
        physics.body.setTransform(room.enemySpawn, 0f);
        physics.body.setLinearVelocity(0f, 0f);
        position.x = room.enemySpawn.x;
        position.y = room.enemySpawn.y;
    }

    private void drawDoors() {
        boolean unlocked = clearedRooms[currentRoomIndex];
        GeneratedRoom generatedRoom = generatedLayout.getRoom(currentRoomIndex);

        for (GridDirection direction : generatedRoom.connections.keySet()) {
            setDoorColor(direction, unlocked);
            Rectangle door = room.doors.get(direction);
            shapeRenderer.rect(door.x, door.y, door.width, door.height);
        }

        if (generatedRoom.exit && unlocked) {
            shapeRenderer.setColor(1f, 0.78f, 0.05f, 1f);
            shapeRenderer.rect(
                room.width / 2f - 0.5f,
                room.height / 2f - 0.5f,
                1f,
                1f
            );
        }
    }

    private void setDoorColor(GridDirection direction, boolean unlocked) {
        if (!unlocked) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            return;
        }

        switch (direction) {
            case NORTH:
                shapeRenderer.setColor(0.75f, 0.2f, 0.9f, 1f);
                break;
            case SOUTH:
                shapeRenderer.setColor(1f, 0.5f, 0.1f, 1f);
                break;
            case EAST:
                shapeRenderer.setColor(0.1f, 0.8f, 0.25f, 1f);
                break;
            case WEST:
                shapeRenderer.setColor(0.15f, 0.55f, 1f, 1f);
                break;
        }
    }

    private void logCurrentRoom() {
        GeneratedRoom generatedRoom = generatedLayout.getRoom(currentRoomIndex);
        Gdx.app.log(
            "RoomTransition",
            "Entered room " + generatedRoom.id
                + " at grid (" + generatedRoom.position.x
                + ", " + generatedRoom.position.y + ")"
                + " type=" + generatedRoom.type
                + (generatedRoom.exit ? " [EXIT]" : "")
        );
    }

    private void configureEnemyForRoom(
        RoomType type,
        HealthComponent health,
        EnemyAIComponent ai
    ) {
        if (type == RoomType.ELITE) {
            health.maximum = 90f;
            ai.movementSpeed = 2.8f;
            ai.attackDamage = 25f;
        } else {
            health.maximum = 50f;
            ai.movementSpeed = 2.2f;
            ai.attackDamage = 15f;
        }
    }

    private void setRoomBackgroundColor(RoomType type) {
        switch (type) {
            case START:
                shapeRenderer.setColor(0.06f, 0.2f, 0.24f, 1f);
                break;
            case COMBAT:
                shapeRenderer.setColor(0.16f, 0.07f, 0.08f, 1f);
                break;
            case LOOT:
                shapeRenderer.setColor(0.22f, 0.18f, 0.05f, 1f);
                break;
            case MERCHANT:
                shapeRenderer.setColor(0.16f, 0.08f, 0.22f, 1f);
                break;
            case ELITE:
                shapeRenderer.setColor(0.24f, 0.04f, 0.04f, 1f);
                break;
            case EXIT:
                shapeRenderer.setColor(0.08f, 0.12f, 0.18f, 1f);
                break;
        }
    }

    private void drawEnemy(
        PositionComponent position,
        EnemyAIComponent ai,
        HealthComponent health,
        InvulnerabilityComponent invulnerability
    ) {
        if (invulnerability.isActive()) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(position.x, position.y, 0.45f);
            drawHealthBar(position, health);
            return;
        }

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
        drawHealthBar(position, health);
    }

    private void drawHealthBar(PositionComponent position, HealthComponent health) {
        float barWidth = 1.1f;
        float healthRatio = health.current / health.maximum;
        float left = position.x - barWidth / 2f;
        float bottom = position.y + 0.65f;

        shapeRenderer.setColor(0.2f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(left, bottom, barWidth, 0.12f);
        shapeRenderer.setColor(0.15f, 0.9f, 0.2f, 1f);
        shapeRenderer.rect(left, bottom, barWidth * healthRatio, 0.12f);
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
