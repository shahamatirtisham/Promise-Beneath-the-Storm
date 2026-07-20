package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.CollectableComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ProjectileComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RangedEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HeavyEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InputSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AimSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.AttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAISystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DamageSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DeathSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.InvulnerabilitySystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.CollectionSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.MerchantSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PlayerDeathSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DashSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.DefenseSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.KnockbackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.ProjectileSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.RangedAttackSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.RangedMovementSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomLoader;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.DungeonLayout;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomAccretionGenerator;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GeneratedRoom;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GridDirection;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomTemplate;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomType;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.EnemySpawnDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.PlayerFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.CollectableFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.MerchantFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PhysicsSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.GameHud;

public class GameScreen implements Screen {
    private Engine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Entity player;
    private final Array<Entity> enemies = new Array<>();
    private final Array<Entity> projectiles = new Array<>();
    private final Array<Entity> collectables = new Array<>();
    private Entity merchant;
    private RoomDefinition room;
    private final Array<Body> roomCollisionBodies = new Array<>();
    private static final String ROOM_TEMPLATE_A = "maps/level1/placeholder_room.tmx";
    private static final String ROOM_TEMPLATE_B = "maps/level1/placeholder_room_b.tmx";
    private boolean[] clearedRooms;
    private boolean[] rewardSpawnedRooms;
    private boolean[] rewardCollectedRooms;
    private boolean[] merchantPurchasedRooms;
    private int currentRoomIndex;
    private DungeonLayout generatedLayout;
    private int levelNumber = 1;
    private boolean levelComplete;
    private boolean debugRenderingEnabled;
    private GameHud hud;

    // Box2D
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private static final float AIM_INDICATOR_DISTANCE = 1.1f;
    private static final float AIM_INDICATOR_RADIUS = 0.12f;
    private static final int MAX_LEVEL = 6;
    private static final int BASE_ROOM_COUNT = 5;
    private static final float BETWEEN_LEVEL_HEAL_RATIO = 0.15f;

    public GameScreen() {
        engine = new Engine();
        camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        viewport = new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        generateDungeonLayout();
        Gdx.app.log("DungeonGenerator", "\n" + generatedLayout.toDebugString());
        logCurrentRoom();
        logLevelStart();
        room = RoomLoader.load(generatedLayout.getRoom(currentRoomIndex).templatePath);

        // Initialize Box2D world (no gravity for top-down)
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        createRoomCollisionBodies();

        player = PlayerFactory.create(world, room.playerSpawn);
        engine.addEntity(player);

        spawnEnemiesForCurrentRoom();

        // AI and input choose velocities before the physics system applies them.
        engine.addSystem(new InputSystem());
        engine.addSystem(new DefenseSystem());
        engine.addSystem(new DashSystem());
        engine.addSystem(new EnemyAISystem(player));
        engine.addSystem(new RangedMovementSystem(player));
        engine.addSystem(new RangedAttackSystem(engine, player, projectiles));
        engine.addSystem(new ProjectileSystem(engine, player, projectiles));
        engine.addSystem(new KnockbackSystem());
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AimSystem(viewport));
        engine.addSystem(new AttackSystem());
        engine.addSystem(new InvulnerabilitySystem());
        engine.addSystem(new DamageSystem(player));
        engine.addSystem(new EnemyAttackSystem(player));
        engine.addSystem(new PlayerDeathSystem());
        engine.addSystem(new DeathSystem(player));
        engine.addSystem(new CollectionSystem(player));
        engine.addSystem(new MerchantSystem(player));
        spawnRoomRewardIfAvailable();
        spawnMerchantIfAvailable();
        hud = new GameHud();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debugRenderingEnabled = !debugRenderingEnabled;
            Gdx.app.log(
                "DebugView",
                debugRenderingEnabled ? "Debug rendering enabled" : "Debug rendering disabled"
            );
        }

        // Input runs first; physics then applies velocity and synchronizes position.
        engine.update(delta);

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartCurrentRoom();
        }

        if (!clearedRooms[currentRoomIndex] && areAllEnemiesDead()) {
            clearedRooms[currentRoomIndex] = true;
            removeCurrentProjectiles();
        }
        updateCollectedRewards();
        updateMerchantState();
        spawnRoomRewardIfAvailable();

        if (levelComplete
            && levelNumber < MAX_LEVEL
            && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startNextLevel();
        } else {
            handleRoomTransition();
            handleLevelCompletion();
        }

        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        FacingComponent playerFacing = player.getComponent(FacingComponent.class);
        AttackComponent playerAttack = player.getComponent(AttackComponent.class);
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent playerInvulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        DashComponent playerDash = player.getComponent(DashComponent.class);
        DefenseComponent playerDefense = player.getComponent(DefenseComponent.class);

        // Camera follows player
        camera.position.set(playerPos.x, playerPos.y, 0);
        camera.update();

        // The HUD uses the full window. Restore the world's fitted viewport before
        // drawing so resizing cannot stretch the game camera.
        viewport.apply();

        float aimX = playerPos.x + playerFacing.x * AIM_INDICATOR_DISTANCE;
        float aimY = playerPos.y + playerFacing.y * AIM_INDICATOR_DISTANCE;

        // Draw filled placeholder graphics.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        setRoomBackgroundColor(generatedLayout.getRoom(currentRoomIndex).type);
        shapeRenderer.rect(0f, 0f, room.width, room.height);

        drawDoors();

        // Player flashes white after taking a hit.
        if (playerState.dead) {
            shapeRenderer.setColor(0.35f, 0.05f, 0.05f, 1f);
        } else if (levelComplete) {
            shapeRenderer.setColor(1f, 0.78f, 0.05f, 1f);
        } else if (playerDash.isActive()) {
            shapeRenderer.setColor(0.15f, 0.75f, 1f, 1f);
        } else if (playerDefense.feedbackTimeRemaining > 0f) {
            shapeRenderer.setColor(0.2f, 1f, 0.35f, 1f);
        } else if (playerDefense.isParryActive()) {
            shapeRenderer.setColor(1f, 0.9f, 0.15f, 1f);
        } else if (playerDefense.blocking) {
            shapeRenderer.setColor(0.6f, 0.25f, 1f, 1f);
        } else if (playerInvulnerability.isActive()) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        } else {
            shapeRenderer.setColor(0f, 1f, 0f, 1f);
        }
        shapeRenderer.circle(playerPos.x, playerPos.y, 0.4f);

        if (debugRenderingEnabled) {
            // Cyan dot shows the world-space direction derived from the mouse.
            shapeRenderer.setColor(0, 1, 1, 1);
            shapeRenderer.circle(aimX, aimY, AIM_INDICATOR_RADIUS);
        }

        if (playerAttack.isActive()) {
            drawAttackArea(playerPos, playerFacing, playerAttack);
        }

        for (Entity collectable : collectables) {
            PositionComponent position = collectable.getComponent(PositionComponent.class);
            shapeRenderer.setColor(1f, 0.82f, 0.05f, 1f);
            shapeRenderer.circle(position.x, position.y, 0.24f);
        }

        if (merchant != null) {
            MerchantComponent merchantData = merchant.getComponent(MerchantComponent.class);
            PositionComponent position = merchant.getComponent(PositionComponent.class);
            if (merchantData.purchased) {
                shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
            } else {
                shapeRenderer.setColor(0.85f, 0.25f, 1f, 1f);
            }
            shapeRenderer.rect(position.x - 0.35f, position.y - 0.35f, 0.7f, 0.7f);
        }

        for (Entity enemy : enemies) {
            drawEnemy(
                enemy.getComponent(PositionComponent.class),
                enemy.getComponent(EnemyAIComponent.class),
                enemy.getComponent(HealthComponent.class),
                enemy.getComponent(InvulnerabilityComponent.class),
                enemy.getComponent(RangedEnemyComponent.class) != null,
                enemy.getComponent(HeavyEnemyComponent.class) != null
            );
        }

        for (Entity projectile : projectiles) {
            PositionComponent position = projectile.getComponent(PositionComponent.class);
            ProjectileComponent data = projectile.getComponent(ProjectileComponent.class);
            shapeRenderer.setColor(1f, 0.12f, 0.05f, 1f);
            shapeRenderer.circle(position.x, position.y, data.radius);
        }

        shapeRenderer.end();

        // Heavy attacks show their real damage radius during the long wind-up.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Entity enemy : enemies) {
            HeavyEnemyComponent heavy = enemy.getComponent(HeavyEnemyComponent.class);
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (heavy == null || ai.state != EnemyAIComponent.State.ATTACK) {
                continue;
            }
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            shapeRenderer.setColor(1f, 0.25f, 0.05f, 1f);
            shapeRenderer.circle(position.x, position.y, ai.attackRange + 0.4f, 48);
        }
        shapeRenderer.end();

        if (debugRenderingEnabled) {
            // Draw debug outlines separately so the room is not filled in.
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 1, 1);
            shapeRenderer.line(playerPos.x, playerPos.y, aimX, aimY);

            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(0f, 0f, room.width, room.height);

            for (Entity enemy : enemies) {
                EnemyAIComponent enemyAI = enemy.getComponent(EnemyAIComponent.class);
                if (enemyAI.state == EnemyAIComponent.State.DEAD) {
                    continue;
                }
                PositionComponent enemyPosition = enemy.getComponent(PositionComponent.class);
                shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
                shapeRenderer.circle(enemyPosition.x, enemyPosition.y, enemyAI.detectionRange, 48);
            }

            shapeRenderer.end();

            // Draw Box2D debug (shows collision shapes).
            debugRenderer.render(world, camera.combined);
        }

        hud.update(
            playerHealth,
            player.getComponent(RunInventoryComponent.class),
            playerDash,
            playerAttack,
            playerDefense,
            levelNumber,
            MAX_LEVEL,
            levelComplete
        );
        hud.render(delta);
    }

    private void createRoomCollisionBodies() {
        for (Rectangle collision : room.collisionRectangles) {
            roomCollisionBodies.add(WorldUtils.createStaticRectangle(world, collision));
        }
    }

    private void generateDungeonLayout() {
        generatedLayout = new RoomAccretionGenerator(
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.START),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.COMBAT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.LOOT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.MERCHANT),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.ELITE),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.EXIT)
        ).generate(getRoomCountForCurrentLevel(), System.currentTimeMillis());

        clearedRooms = new boolean[generatedLayout.rooms.size()];
        rewardSpawnedRooms = new boolean[generatedLayout.rooms.size()];
        rewardCollectedRooms = new boolean[generatedLayout.rooms.size()];
        merchantPurchasedRooms = new boolean[generatedLayout.rooms.size()];
        for (GeneratedRoom generatedRoom : generatedLayout.rooms) {
            clearedRooms[generatedRoom.id] = !generatedRoom.type.requiresClear;
        }
    }

    private void handleLevelCompletion() {
        if (levelComplete) {
            return;
        }

        GeneratedRoom generatedRoom = generatedLayout.getRoom(currentRoomIndex);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        if (!generatedRoom.exit
            || !clearedRooms[currentRoomIndex]
            || !getExitPortal().contains(playerPosition.x, playerPosition.y)) {
            return;
        }

        levelComplete = true;
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        playerState.controlsLocked = true;
        VelocityComponent velocity = player.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;

        RunInventoryComponent inventory =
            player.getComponent(RunInventoryComponent.class);
        Gdx.app.log(
            "LevelComplete",
            "Level " + levelNumber + " complete | Rooms: "
                + countClearedRooms() + "/" + clearedRooms.length
                + " | Enemies defeated: " + inventory.enemiesDefeated
                + " | Devil Coins: " + inventory.devilCoins
                + (levelNumber < MAX_LEVEL
                    ? " | Press Enter for the next level"
                    : " | All 6 levels complete - boss gauntlet is next")
        );
    }

    private void startNextLevel() {
        removeCurrentProjectiles();
        removeCurrentEnemies();
        removeCurrentCollectables();
        removeCurrentMerchant();
        for (Body body : roomCollisionBodies) {
            world.destroyBody(body);
        }
        roomCollisionBodies.clear();

        levelNumber++;
        levelComplete = false;
        currentRoomIndex = 0;
        generateDungeonLayout();
        room = RoomLoader.load(generatedLayout.getRoom(0).templatePath);
        createRoomCollisionBodies();
        resetPlayerForNewLevel();
        spawnEnemiesForCurrentRoom();
        spawnRoomRewardIfAvailable();
        spawnMerchantIfAvailable();

        Gdx.app.log("DungeonGenerator", "\n" + generatedLayout.toDebugString());
        logCurrentRoom();
        logLevelStart();
    }

    private int getRoomCountForCurrentLevel() {
        return BASE_ROOM_COUNT + levelNumber;
    }

    private void logLevelStart() {
        Gdx.app.log(
            "Level",
            "Level " + levelNumber + " started with "
                + generatedLayout.rooms.size()
                + " rooms."
        );
    }

    private void resetPlayerForNewLevel() {
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        playerState.dead = false;
        playerState.controlsLocked = false;

        HealthComponent health = player.getComponent(HealthComponent.class);
        health.current = Math.min(
            health.maximum,
            health.current + health.maximum * BETWEEN_LEVEL_HEAL_RATIO
        );

        VelocityComponent velocity = player.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;

        Body playerBody = player.getComponent(PhysicsComponent.class).body;
        playerBody.setTransform(room.playerSpawn, 0f);
        playerBody.setLinearVelocity(0f, 0f);
        PositionComponent position = player.getComponent(PositionComponent.class);
        position.x = room.playerSpawn.x;
        position.y = room.playerSpawn.y;

        AttackComponent attack = player.getComponent(AttackComponent.class);
        attack.activeTimeRemaining = 0f;
        attack.cooldownRemaining = 0f;
        attack.comboStep = -1;
        attack.comboResetRemaining = 0f;

        DashComponent dash = player.getComponent(DashComponent.class);
        dash.activeTimeRemaining = 0f;
        dash.cooldownRemaining = 0f;

        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        defense.blocking = false;
        defense.parryTimeRemaining = 0f;
        defense.feedbackTimeRemaining = 0f;
    }

    private Rectangle getExitPortal() {
        return new Rectangle(
            room.width / 2f - 0.5f,
            room.height / 2f - 0.5f,
            1f,
            1f
        );
    }

    private int countClearedRooms() {
        int count = 0;
        for (boolean cleared : clearedRooms) {
            if (cleared) {
                count++;
            }
        }
        return count;
    }

    private void handleRoomTransition() {
        if (!clearedRooms[currentRoomIndex]
            || player.getComponent(PlayerComponent.class).dead) {
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
        removeCurrentProjectiles();
        removeCurrentEnemies();
        removeCurrentCollectables();
        removeCurrentMerchant();

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

        spawnEnemiesForCurrentRoom();
        spawnRoomRewardIfAvailable();
        spawnMerchantIfAvailable();
    }

    private void spawnEnemiesForCurrentRoom() {
        if (clearedRooms[currentRoomIndex]) {
            return;
        }

        RoomType roomType = generatedLayout.getRoom(currentRoomIndex).type;
        int spawnLimit = roomType == RoomType.ELITE ? 2 : room.enemySpawns.size;
        for (int index = 0; index < spawnLimit; index++) {
            EnemySpawnDefinition spawn = room.enemySpawns.get(index);
            Entity enemy;
            if (spawn.type == EnemySpawnDefinition.Type.RANGED) {
                enemy = EnemyFactory.createRanged(world, spawn.position);
            } else if (spawn.type == EnemySpawnDefinition.Type.HEAVY) {
                enemy = EnemyFactory.createHeavy(world, spawn.position);
            } else {
                enemy = EnemyFactory.createMelee(world, spawn.position);
            }
            configureEnemyForRoom(
                roomType,
                enemy.getComponent(HealthComponent.class),
                enemy.getComponent(EnemyAIComponent.class)
            );
            HealthComponent health = enemy.getComponent(HealthComponent.class);
            if (enemy.getComponent(HeavyEnemyComponent.class) != null) {
                configureHeavyEnemy(health, enemy.getComponent(EnemyAIComponent.class));
            }
            health.current = health.maximum;
            enemies.add(enemy);
            engine.addEntity(enemy);
        }
    }

    private void removeCurrentEnemies() {
        for (Entity enemy : enemies) {
            PhysicsComponent physics = enemy.getComponent(PhysicsComponent.class);
            engine.removeEntity(enemy);
            world.destroyBody(physics.body);
        }
        enemies.clear();
    }

    private void removeCurrentProjectiles() {
        for (Entity projectile : projectiles) {
            engine.removeEntity(projectile);
        }
        projectiles.clear();
    }

    private void restartCurrentRoom() {
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        HealthComponent health = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent invulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        VelocityComponent velocity = player.getComponent(VelocityComponent.class);
        AttackComponent attack = player.getComponent(AttackComponent.class);
        DashComponent dash = player.getComponent(DashComponent.class);
        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

        playerState.dead = false;
        health.current = health.maximum;
        invulnerability.timeRemaining = 0.75f;
        velocity.vx = 0f;
        velocity.vy = 0f;
        attack.activeTimeRemaining = 0f;
        attack.cooldownRemaining = 0f;
        attack.comboStep = -1;
        attack.comboResetRemaining = 0f;
        dash.activeTimeRemaining = 0f;
        dash.cooldownRemaining = 0f;
        defense.blocking = false;
        defense.parryTimeRemaining = 0f;
        defense.feedbackTimeRemaining = 0f;
        physics.body.setTransform(room.playerSpawn, 0f);
        physics.body.setLinearVelocity(0f, 0f);

        PositionComponent position = player.getComponent(PositionComponent.class);
        position.x = room.playerSpawn.x;
        position.y = room.playerSpawn.y;

        removeCurrentProjectiles();
        removeCurrentEnemies();
        spawnEnemiesForCurrentRoom();
        Gdx.app.log("Player", "Current room restarted.");
    }

    private boolean areAllEnemiesDead() {
        if (enemies.size == 0) {
            return true;
        }
        for (Entity enemy : enemies) {
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (ai.state != EnemyAIComponent.State.DEAD) {
                return false;
            }
        }
        return true;
    }

    private void spawnRoomRewardIfAvailable() {
        if (rewardSpawnedRooms[currentRoomIndex]
            || rewardCollectedRooms[currentRoomIndex]) {
            return;
        }

        RoomType type = generatedLayout.getRoom(currentRoomIndex).type;
        boolean shouldSpawn = type == RoomType.LOOT
            || (type == RoomType.ELITE && clearedRooms[currentRoomIndex]);
        if (!shouldSpawn) {
            return;
        }

        int value = type == RoomType.ELITE ? 10 : 5;
        for (Vector2 spawn : room.lootSpawns) {
            Entity collectable = CollectableFactory.createDevilCoins(spawn, value);
            collectables.add(collectable);
            engine.addEntity(collectable);
        }
        rewardSpawnedRooms[currentRoomIndex] = true;
    }

    private void updateCollectedRewards() {
        for (int index = collectables.size - 1; index >= 0; index--) {
            Entity collectable = collectables.get(index);
            if (!collectable.getComponent(CollectableComponent.class).collected) {
                continue;
            }
            rewardCollectedRooms[currentRoomIndex] = true;
            engine.removeEntity(collectable);
            collectables.removeIndex(index);
        }
    }

    private void removeCurrentCollectables() {
        for (Entity collectable : collectables) {
            engine.removeEntity(collectable);
        }
        collectables.clear();
        if (!rewardCollectedRooms[currentRoomIndex]) {
            rewardSpawnedRooms[currentRoomIndex] = false;
        }
    }

    private void spawnMerchantIfAvailable() {
        RoomType type = generatedLayout.getRoom(currentRoomIndex).type;
        if (type != RoomType.MERCHANT || merchantPurchasedRooms[currentRoomIndex]) {
            return;
        }

        merchant = MerchantFactory.createHealthMerchant(room.merchantSpawn, 5);
        engine.addEntity(merchant);
        Gdx.app.log(
            "Merchant",
            "Approach the purple merchant and press E. Cost: 5 Devil Coins"
        );
    }

    private void updateMerchantState() {
        if (merchant == null) {
            return;
        }
        MerchantComponent merchantData = merchant.getComponent(MerchantComponent.class);
        if (merchantData.purchased) {
            merchantPurchasedRooms[currentRoomIndex] = true;
        }
    }

    private void removeCurrentMerchant() {
        if (merchant == null) {
            return;
        }
        engine.removeEntity(merchant);
        merchant = null;
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
            Rectangle portal = getExitPortal();
            shapeRenderer.rect(portal.x, portal.y, portal.width, portal.height);
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
        float difficultyMultiplier = 1f + (levelNumber - 1) * 0.15f;
        if (type == RoomType.ELITE) {
            health.maximum = 90f * difficultyMultiplier;
            ai.movementSpeed = 2.8f + (levelNumber - 1) * 0.1f;
            ai.attackDamage = 25f * difficultyMultiplier;
        } else {
            health.maximum = 50f * difficultyMultiplier;
            ai.movementSpeed = 2.2f + (levelNumber - 1) * 0.08f;
            ai.attackDamage = 15f * difficultyMultiplier;
        }
    }

    private void configureHeavyEnemy(HealthComponent health, EnemyAIComponent ai) {
        health.maximum *= 2f;
        ai.movementSpeed *= 0.6f;
        ai.attackRange = 1.8f;
        ai.attackWindup = 0.85f;
        ai.recoveryDuration = 1.1f;
        ai.attackDamage *= 1.6f;
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
        InvulnerabilityComponent invulnerability,
        boolean ranged,
        boolean heavy
    ) {
        float radius = heavy ? 0.65f : 0.45f;
        if (invulnerability.isActive()) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(position.x, position.y, radius);
            drawHealthBar(position, health);
            return;
        }

        if (heavy && ai.state != EnemyAIComponent.State.DEAD) {
            shapeRenderer.setColor(0.65f, 0.28f, 0.08f, 1f);
        } else if (ranged && ai.state != EnemyAIComponent.State.DEAD) {
            shapeRenderer.setColor(0.75f, 0.15f, 0.9f, 1f);
        } else switch (ai.state) {
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
            case STUNNED:
                shapeRenderer.setColor(0.15f, 0.45f, 1f, 1f);
                break;
            case DEAD:
                shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
                break;
        }
        shapeRenderer.circle(position.x, position.y, radius);
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

        if (attack.comboStep == 2) {
            shapeRenderer.setColor(1f, 0.15f, 0.05f, 1f);
        } else if (attack.comboStep == 1) {
            shapeRenderer.setColor(1f, 0.5f, 0.05f, 1f);
        } else {
            shapeRenderer.setColor(1f, 0.85f, 0.1f, 1f);
        }
        shapeRenderer.triangle(startX, startY, leftX, leftY, rightX, rightY);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        debugRenderer.dispose();
        world.dispose();
        hud.dispose();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
