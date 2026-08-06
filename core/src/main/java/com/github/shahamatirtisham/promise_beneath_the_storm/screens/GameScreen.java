package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.LeverComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChestKeyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.KeyCarrierComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChargerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.SlowZoneComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ResurrectionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.NecromancerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ExplosiveBarrelComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HazardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.KnifeDropComponent;
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
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.ChestSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.LeverSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.KeyCollectionSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.BossPhaseSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.BossCombatSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.ChargerSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.WaterSlowSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.NecromancerSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.ExplosiveBarrelSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.ShieldGuardSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.StatusEffectSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.StatusEffectApplicator;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PlayerRangedSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.HazardSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomLoader;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.DungeonLayout;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomAccretionGenerator;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GeneratedRoom;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.GridDirection;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomTemplate;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomType;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.EnemySpawnDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.EncounterDirector;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.LevelTheme;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.PlayerFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.CollectableFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.MerchantFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.ChestFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.LeverFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.KeyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnvironmentFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PhysicsSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.GameHud;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.RunCheckpoint;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

public class GameScreen implements Screen {
    private final Main game;
    private Engine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Entity player;
    private final Array<Entity> enemies = new Array<>();
    private final Array<Entity> projectiles = new Array<>();
    private final Array<Entity> collectables = new Array<>();
    private final Array<Entity> chestKeys = new Array<>();
    private final Array<Entity> environmentZones = new Array<>();
    private final Array<Entity> explosiveBarrels = new Array<>();
    private final Array<Entity> hazards = new Array<>();
    private Entity merchant;
    private Entity chest;
    private Entity lever;
    private Entity boss;
    private RoomDefinition room;
    private final Array<Body> roomCollisionBodies = new Array<>();
    private static final String ROOM_TEMPLATE_A = "maps/level1/placeholder_room.tmx";
    private static final String ROOM_TEMPLATE_B = "maps/level1/placeholder_room_b.tmx";
    private boolean[] clearedRooms;
    private boolean[] rewardSpawnedRooms;
    private boolean[] rewardCollectedRooms;
    private boolean[] chestOpenedRooms;
    private boolean[] leverActivatedRooms;
    private boolean[] keyDroppedRooms;
    private boolean[] keyCollectedRooms;
    private boolean[] keySpawnedRooms;
    private CollectableComponent.Type[] roomRewardTypes;
    private int[] roomRewardValues;
    private RelicType[] roomRewardRelics;
    private boolean[] bonusKnifeAvailableRooms;
    private boolean[] bonusKnifeSpawnedRooms;
    private boolean[] bonusKnifeCollectedRooms;
    private int[] merchantPurchasedMasks;
    private int currentRoomIndex;
    private DungeonLayout generatedLayout;
    private long dungeonSeed;
    private LevelTheme currentTheme;
    private int levelNumber = 1;
    private boolean levelComplete;
    private boolean bossMode;
    private boolean bossVictory;
    private int checkpointReached;
    private final RunCheckpoint checkpoint = new RunCheckpoint();
    private boolean debugRenderingEnabled;
    private boolean hazardDebugEnabled;
    private int debugStatusLevel = 2;
    private GameHud hud;

    // Box2D
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private static final float AIM_INDICATOR_DISTANCE = 1.1f;
    private static final float AIM_INDICATOR_RADIUS = 0.12f;
    private static final int MAX_LEVEL = 6;
    private static final int BASE_ROOM_COUNT = 5;
    private static final float BETWEEN_LEVEL_HEAL_RATIO = 0.15f;

    public GameScreen(Main game) {
        this(game, null);
    }

    public GameScreen(Main game, RunCheckpoint savedCheckpoint) {
        this.game = game;
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
        captureCheckpoint(1, false, 0);
        spawnEnvironmentForCurrentRoom();

        spawnEnemiesForCurrentRoom();

        // AI and input choose velocities before the physics system applies them.
        engine.addSystem(new InputSystem());
        engine.addSystem(new WaterSlowSystem(player, environmentZones));
        engine.addSystem(new HazardSystem(player, hazards));
        engine.addSystem(new DefenseSystem());
        engine.addSystem(new DashSystem());
        engine.addSystem(new EnemyAISystem(player));
        engine.addSystem(new BossCombatSystem(engine, player, projectiles));
        engine.addSystem(new ShieldGuardSystem(player));
        engine.addSystem(new ChargerSystem(player));
        engine.addSystem(new RangedMovementSystem(player));
        engine.addSystem(new RangedAttackSystem(engine, player, projectiles));
        engine.addSystem(new PlayerRangedSystem(engine, projectiles));
        engine.addSystem(new ProjectileSystem(
            engine,
            player,
            projectiles,
            enemies,
            () -> levelNumber
        ));
        engine.addSystem(new KnockbackSystem());
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AimSystem(viewport));
        engine.addSystem(new AttackSystem(
            () -> hud != null && hud.isPointerOverPauseButton()
        ));
        engine.addSystem(new ExplosiveBarrelSystem(player, enemies, explosiveBarrels));
        engine.addSystem(new StatusEffectSystem());
        engine.addSystem(new InvulnerabilitySystem());
        engine.addSystem(new DamageSystem(player));
        engine.addSystem(new BossPhaseSystem());
        engine.addSystem(new EnemyAttackSystem(player, () -> levelNumber));
        engine.addSystem(new PlayerDeathSystem());
        engine.addSystem(new DeathSystem(player));
        engine.addSystem(new NecromancerSystem(player));
        engine.addSystem(new CollectionSystem(player));
        engine.addSystem(new ChestSystem(engine, player, collectables));
        engine.addSystem(new LeverSystem(player));
        engine.addSystem(new KeyCollectionSystem(player));
        engine.addSystem(new MerchantSystem(player));
        spawnLeverIfAvailable();
        spawnRoomRewardIfAvailable();
        spawnBonusKnifeIfAvailable();
        spawnMerchantIfAvailable();
        hud = new GameHud(
            () -> {
                hud.closePauseMenu();
                if (bossMode) startBossEncounter();
                else restoreLatestCheckpoint();
            },
            this::returnToMainMenu
        );
        if (savedCheckpoint != null) {
            checkpoint.capture(savedCheckpoint.restartLevel, savedCheckpoint.bossCheckpoint,
                savedCheckpoint.maximumHealth, savedCheckpoint.devilCoins,
                savedCheckpoint.enemiesDefeated, savedCheckpoint.ironHeart,
                savedCheckpoint.stormEdge, savedCheckpoint.windstepSigil,
                savedCheckpoint.attackDamage, savedCheckpoint.dashCooldown,
                savedCheckpoint.knives, savedCheckpoint.knifeCapacity);
            restoreLatestCheckpoint();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (hud.handleEscape()) {
                return;
            }
        }

        if (!hud.isPaused() && !hud.consumeGameplayInputBlock()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debugRenderingEnabled = !debugRenderingEnabled;
            Gdx.app.log(
                "DebugView",
                debugRenderingEnabled ? "Debug rendering enabled" : "Debug rendering disabled"
            );
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            RunInventoryComponent inventory =
                player.getComponent(RunInventoryComponent.class);
            inventory.devilCoins += 25;
            Gdx.app.log("DebugView", "Granted 25 test Devil Coins");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            RelicInventoryComponent relics =
                player.getComponent(RelicInventoryComponent.class);
            RelicType[] relicTypes = RelicType.values();
            RelicType relic = relicTypes[relics.total() % relicTypes.length];
            relic.apply(player);
            Gdx.app.log("DebugView", "Granted test relic: " + relic.displayName);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            StatusEffectApplicator.applyForLevel(player, debugStatusLevel);
            Gdx.app.log(
                "DebugView",
                "Applied Level " + debugStatusLevel + " test status"
            );
            debugStatusLevel = debugStatusLevel >= MAX_LEVEL
                ? 2
                : debugStatusLevel + 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            hazardDebugEnabled = !hazardDebugEnabled;
            Gdx.app.log(
                "DebugView",
                hazardDebugEnabled
                    ? "Hazard bounds enabled"
                    : "Hazard bounds disabled"
            );
        }
        if (!bossMode && Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            Gdx.app.log("DebugView", "Skipping to boss encounter");
            captureCheckpoint(MAX_LEVEL, true, 2);
            startBossEncounter();
        }
        // F11 belongs to the global fullscreen toggle in Main.
        if (bossMode && boss != null && Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            BossComponent bossData = boss.getComponent(BossComponent.class);
            HealthComponent bossHealth = boss.getComponent(HealthComponent.class);
            EnemyAIComponent bossAi = boss.getComponent(EnemyAIComponent.class);
            bossData.phase = BossComponent.Phase.IRHOS_REVEALED;
            bossData.transitionTimeRemaining = 0f;
            bossData.attackCycleReady = false;
            bossData.revealedConfigured = false;
            bossHealth.current = bossHealth.maximum * 0.25f;
            boss.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
            bossAi.state = EnemyAIComponent.State.CHASE;
            bossAi.attackPending = false;
            Gdx.app.log("DebugView", "Forced Irhos Revealed test phase");
        }
        if (!bossMode && levelNumber < MAX_LEVEL
            && Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            Gdx.app.log("DebugView", "Skipping to next level theme");
            if (levelNumber == 3) {
                captureCheckpoint(4, false, 1);
            }
            startNextLevel();
        }
        if (!bossMode && !levelComplete
            && Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            levelComplete = true;
            checkpointReached = levelNumber == 3
                ? 1
                : levelNumber == MAX_LEVEL ? 2 : 0;
            PlayerComponent debugPlayerState =
                player.getComponent(PlayerComponent.class);
            debugPlayerState.controlsLocked = true;
            VelocityComponent debugVelocity = player.getComponent(VelocityComponent.class);
            debugVelocity.vx = 0f;
            debugVelocity.vy = 0f;
            Gdx.app.log("DebugView", "Forced level completion for Storm Boon test");
        }

        // Input runs first; physics then applies velocity and synchronizes position.
        engine.update(delta);

        if (playerState.dead && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            if (bossMode) {
                startBossEncounter();
            } else {
                restoreLatestCheckpoint();
            }
        }

        if (bossMode) {
            updateBossVictory();
            if (bossVictory) {
                game.showVictory(this);
                return;
            }
        } else {
            if (!clearedRooms[currentRoomIndex] && areAllEnemiesDead()) {
                clearedRooms[currentRoomIndex] = true;
                removeCurrentProjectiles();
            }
            spawnDroppedKnives();
            updateCollectedRewards();
            updateChestState();
            updateLeverState();
            updateKeyState();
            updateMerchantState();
            spawnDroppedChestKey();
            spawnRoomKeyIfAvailable();
            spawnRoomRewardIfAvailable();
            spawnBonusKnifeIfAvailable();

            if (levelComplete && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.showLevelUpgrade(this);
                return;
            } else {
                handleRoomTransition();
                handleLevelCompletion();
            }
        }
        }

        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        hud.setGameOver(playerState.dead);
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

        for (Entity zone : environmentZones) {
            SlowZoneComponent water = zone.getComponent(SlowZoneComponent.class);
            shapeRenderer.setColor(0.05f, 0.35f, 0.65f, 1f);
            shapeRenderer.rect(
                water.bounds.x,
                water.bounds.y,
                water.bounds.width,
                water.bounds.height
            );
        }
        for (Entity barrel : explosiveBarrels) {
            PositionComponent position = barrel.getComponent(PositionComponent.class);
            ExplosiveBarrelComponent data =
                barrel.getComponent(ExplosiveBarrelComponent.class);
            if (data.explosionTimeRemaining > 0f) {
                shapeRenderer.setColor(1f, 0.35f, 0.02f, 1f);
                shapeRenderer.circle(position.x, position.y, data.explosionRadius);
            } else if (data.destroyed && !data.explosionApplied) {
                shapeRenderer.setColor(1f, 0.8f, 0.05f, 1f);
                shapeRenderer.rect(position.x - 0.35f, position.y - 0.45f, 0.7f, 0.9f);
            } else if (!data.destroyed) {
                shapeRenderer.setColor(0.65f, 0.12f, 0.03f, 1f);
                shapeRenderer.rect(position.x - 0.35f, position.y - 0.45f, 0.7f, 0.9f);
            }
        }
        for (Entity hazardEntity : hazards) {
            HazardComponent hazard =
                hazardEntity.getComponent(HazardComponent.class);
            if (hazard.type == HazardComponent.Type.POISON_POOL) {
                shapeRenderer.setColor(0.22f, 0.6f, 0.08f, 0.75f);
            } else if (hazard.active) {
                shapeRenderer.setColor(
                    hazard.type == HazardComponent.Type.SPIKES
                        ? 0.85f : 1f,
                    hazard.type == HazardComponent.Type.SPIKES
                        ? 0.85f : 0.25f,
                    hazard.type == HazardComponent.Type.SPIKES
                        ? 0.9f : 0.02f,
                    1f
                );
            } else if (hazard.isWarning()) {
                shapeRenderer.setColor(1f, 0.72f, 0.05f, 0.8f);
            } else {
                shapeRenderer.setColor(0.18f, 0.16f, 0.16f, 0.45f);
            }
            shapeRenderer.rect(
                hazard.bounds.x,
                hazard.bounds.y,
                hazard.bounds.width,
                hazard.bounds.height
            );
        }

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
            CollectableComponent data =
                collectable.getComponent(CollectableComponent.class);
            switch (data.type) {
                case HEAL:
                    shapeRenderer.setColor(0.15f, 1f, 0.25f, 1f);
                    break;
                case MAX_HEALTH:
                    shapeRenderer.setColor(0.1f, 0.85f, 1f, 1f);
                    break;
                case RELIC:
                    shapeRenderer.setColor(1f, 0.25f, 0.85f, 1f);
                    break;
                case KNIFE:
                    shapeRenderer.setColor(0.75f, 0.9f, 1f, 1f);
                    break;
                case DEVIL_COINS:
                default:
                    shapeRenderer.setColor(1f, 0.82f, 0.05f, 1f);
                    break;
            }
            shapeRenderer.circle(position.x, position.y, 0.24f);
        }

        if (merchant != null) {
            MerchantComponent merchantData = merchant.getComponent(MerchantComponent.class);
            PositionComponent position = merchant.getComponent(PositionComponent.class);
            shapeRenderer.setColor(0.85f, 0.25f, 1f, 1f);
            shapeRenderer.rect(position.x - 0.35f, position.y - 0.35f, 0.7f, 0.7f);
        }

        if (chest != null) {
            ChestComponent chestData = chest.getComponent(ChestComponent.class);
            PositionComponent position = chest.getComponent(PositionComponent.class);
            if (chestData.opened) {
                shapeRenderer.setColor(0.35f, 0.18f, 0.05f, 1f);
            } else if (chestData.unlocked) {
                shapeRenderer.setColor(1f, 0.65f, 0.05f, 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.3f, 0.32f, 1f);
            }
            shapeRenderer.rect(position.x - 0.45f, position.y - 0.3f, 0.9f, 0.6f);
        }

        if (lever != null) {
            LeverComponent leverData = lever.getComponent(LeverComponent.class);
            PositionComponent position = lever.getComponent(PositionComponent.class);
            shapeRenderer.setColor(
                leverData.activated ? 0.2f : 0.15f,
                leverData.activated ? 0.9f : 0.45f,
                leverData.activated ? 0.25f : 1f,
                1f
            );
            shapeRenderer.rect(position.x - 0.2f, position.y - 0.4f, 0.4f, 0.8f);
        }

        for (Entity key : chestKeys) {
            PositionComponent position = key.getComponent(PositionComponent.class);
            shapeRenderer.setColor(0.82f, 0.9f, 1f, 1f);
            shapeRenderer.circle(position.x, position.y, 0.2f);
            shapeRenderer.rect(position.x, position.y - 0.07f, 0.38f, 0.14f);
        }

        for (Entity enemy : enemies) {
            BossComponent bossData = enemy.getComponent(BossComponent.class);
            if (bossData != null) {
                drawBossAttackTelegraph(bossData);
            }
            drawEnemy(
                enemy.getComponent(PositionComponent.class),
                enemy.getComponent(EnemyAIComponent.class),
                enemy.getComponent(HealthComponent.class),
                enemy.getComponent(InvulnerabilityComponent.class),
                enemy.getComponent(RangedEnemyComponent.class) != null,
                enemy.getComponent(HeavyEnemyComponent.class) != null,
                bossData,
                enemy.getComponent(ChargerComponent.class),
                enemy.getComponent(ResurrectionComponent.class),
                enemy.getComponent(NecromancerComponent.class),
                enemy.getComponent(ShieldGuardComponent.class)
            );
        }

        for (Entity projectile : projectiles) {
            PositionComponent position = projectile.getComponent(PositionComponent.class);
            ProjectileComponent data = projectile.getComponent(ProjectileComponent.class);
            TeamComponent team = projectile.getComponent(TeamComponent.class);
            shapeRenderer.setColor(
                team.team == TeamComponent.Team.PLAYER ? 0.7f : 1f,
                team.team == TeamComponent.Team.PLAYER ? 0.85f : 0.12f,
                team.team == TeamComponent.Team.PLAYER ? 1f : 0.05f,
                1f
            );
            shapeRenderer.circle(position.x, position.y, data.radius);
        }

        drawDarknessOverlay(playerPos);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

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
        for (Entity enemy : enemies) {
            ChargerComponent charger = enemy.getComponent(ChargerComponent.class);
            if (charger == null || charger.state != ChargerComponent.State.WINDUP) {
                continue;
            }
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            float chargeDistance = charger.chargeSpeed * charger.chargeDuration;
            shapeRenderer.setColor(1f, 0.05f, 0.05f, 1f);
            shapeRenderer.line(
                position.x,
                position.y,
                position.x + charger.directionX * chargeDistance,
                position.y + charger.directionY * chargeDistance
            );
        }
        for (Entity enemy : enemies) {
            NecromancerComponent necromancer =
                enemy.getComponent(NecromancerComponent.class);
            if (necromancer == null || !necromancer.channeling
                || necromancer.targetCorpse == null) {
                continue;
            }
            PositionComponent source = enemy.getComponent(PositionComponent.class);
            PositionComponent target =
                necromancer.targetCorpse.getComponent(PositionComponent.class);
            shapeRenderer.setColor(0.8f, 0.15f, 1f, 1f);
            shapeRenderer.line(source.x, source.y, target.x, target.y);
        }
        for (Entity enemy : enemies) {
            ShieldGuardComponent shield = enemy.getComponent(ShieldGuardComponent.class);
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (shield == null || shield.isGuardBroken()
                || ai.state == EnemyAIComponent.State.DEAD) {
                continue;
            }
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            float shieldX = position.x + shield.facingX * 0.65f;
            float shieldY = position.y + shield.facingY * 0.65f;
            float perpendicularX = -shield.facingY * 0.45f;
            float perpendicularY = shield.facingX * 0.45f;
            shapeRenderer.setColor(0.15f, 0.75f, 1f, 1f);
            shapeRenderer.line(
                shieldX - perpendicularX,
                shieldY - perpendicularY,
                shieldX + perpendicularX,
                shieldY + perpendicularY
            );
        }
        shapeRenderer.end();

        if (debugRenderingEnabled) {
            // Draw debug outlines separately so the room is not filled in.
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 1, 1);
            shapeRenderer.line(playerPos.x, playerPos.y, aimX, aimY);

            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(0f, 0f, room.width, room.height);

            if (hazardDebugEnabled) {
                shapeRenderer.setColor(1f, 0.2f, 1f, 1f);
                for (Entity hazardEntity : hazards) {
                    HazardComponent hazard =
                        hazardEntity.getComponent(HazardComponent.class);
                    shapeRenderer.rect(
                        hazard.bounds.x,
                        hazard.bounds.y,
                        hazard.bounds.width,
                        hazard.bounds.height
                    );
                }
            }

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
            player.getComponent(RelicInventoryComponent.class),
            player.getComponent(StatusEffectComponent.class),
            playerDash,
            playerAttack,
            playerDefense,
            player.getComponent(PlayerRangedComponent.class),
            merchant == null ? null : merchant.getComponent(MerchantComponent.class),
            isPlayerNearMerchant(),
            levelNumber,
            MAX_LEVEL,
            levelComplete,
            checkpointReached,
            currentTheme.displayName
        );
        hud.updateBoss(
            boss == null ? null : boss.getComponent(BossComponent.class),
            boss == null ? null : boss.getComponent(HealthComponent.class),
            bossVictory
        );
        hud.render(delta);
    }

    private void createRoomCollisionBodies() {
        for (Rectangle collision : room.collisionRectangles) {
            roomCollisionBodies.add(WorldUtils.createStaticRectangle(world, collision));
        }
    }

    private void spawnEnvironmentForCurrentRoom() {
        RoomType roomType = generatedLayout.getRoom(currentRoomIndex).type;
        if (roomType != RoomType.COMBAT && roomType != RoomType.ELITE) {
            return;
        }
        boolean supportsWater = currentTheme.mechanic == LevelTheme.Mechanic.WATER_ZONES
            || currentTheme.mechanic == LevelTheme.Mechanic.COMBINED;
        if (supportsWater) {
            Rectangle waterBounds = new Rectangle(
                room.width / 2f - 3f,
                room.height / 2f - 1.5f,
                6f,
                3f
            );
            Entity water = EnvironmentFactory.createWaterZone(waterBounds);
            environmentZones.add(water);
            engine.addEntity(water);
        }

        boolean supportsBarrels =
            currentTheme.mechanic == LevelTheme.Mechanic.EXPLOSIVE_BARRELS
                || currentTheme.mechanic == LevelTheme.Mechanic.COMBINED;
        if (supportsBarrels) {
            spawnBarrel(room.width / 2f - 0.9f, room.height / 2f);
            spawnBarrel(room.width / 2f + 0.9f, room.height / 2f);
        }

        if (levelNumber == 3 || levelNumber == 6) {
            spawnHazard(
                HazardComponent.Type.POISON_POOL,
                new Rectangle(room.width * 0.62f, room.height * 0.25f, 2.6f, 2f),
                0f
            );
        }
        if (levelNumber == 4 || levelNumber == 6) {
            spawnHazard(
                HazardComponent.Type.FIRE_VENT,
                new Rectangle(room.width * 0.3f, room.height * 0.62f, 2f, 2f),
                (currentRoomIndex * 0.7f) % 4.2f
            );
        }
        if (levelNumber == 5 || levelNumber == 6) {
            spawnHazard(
                HazardComponent.Type.SPIKES,
                new Rectangle(room.width * 0.44f, room.height * 0.18f, 2.4f, 1.4f),
                (currentRoomIndex * 0.55f) % 3.4f
            );
            spawnHazard(
                HazardComponent.Type.SPIKES,
                new Rectangle(room.width * 0.44f, room.height * 0.7f, 2.4f, 1.4f),
                (1.7f + currentRoomIndex * 0.55f) % 3.4f
            );
        }
    }

    private void spawnBarrel(float x, float y) {
        Entity barrel = EnvironmentFactory.createExplosiveBarrel(x, y);
        explosiveBarrels.add(barrel);
        engine.addEntity(barrel);
    }

    private void spawnHazard(
        HazardComponent.Type type,
        Rectangle bounds,
        float startingTime
    ) {
        Entity hazard = EnvironmentFactory.createHazard(type, bounds, startingTime);
        hazards.add(hazard);
        engine.addEntity(hazard);
    }

    private void removeCurrentEnvironment() {
        for (Entity zone : environmentZones) {
            engine.removeEntity(zone);
        }
        environmentZones.clear();
        for (Entity barrel : explosiveBarrels) {
            engine.removeEntity(barrel);
        }
        explosiveBarrels.clear();
        for (Entity hazard : hazards) {
            engine.removeEntity(hazard);
        }
        hazards.clear();
    }

    private void generateDungeonLayout() {
        currentTheme = LevelTheme.forLevel(levelNumber);
        dungeonSeed = System.currentTimeMillis();
        generatedLayout = new RoomAccretionGenerator(
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.START),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.COMBAT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.LOOT),
            new RoomTemplate(ROOM_TEMPLATE_A, RoomType.MERCHANT),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.ELITE),
            new RoomTemplate(ROOM_TEMPLATE_B, RoomType.EXIT)
        ).generate(getRoomCountForCurrentLevel(), dungeonSeed);

        clearedRooms = new boolean[generatedLayout.rooms.size()];
        rewardSpawnedRooms = new boolean[generatedLayout.rooms.size()];
        rewardCollectedRooms = new boolean[generatedLayout.rooms.size()];
        chestOpenedRooms = new boolean[generatedLayout.rooms.size()];
        leverActivatedRooms = new boolean[generatedLayout.rooms.size()];
        keyDroppedRooms = new boolean[generatedLayout.rooms.size()];
        keyCollectedRooms = new boolean[generatedLayout.rooms.size()];
        keySpawnedRooms = new boolean[generatedLayout.rooms.size()];
        roomRewardTypes = new CollectableComponent.Type[generatedLayout.rooms.size()];
        roomRewardValues = new int[generatedLayout.rooms.size()];
        roomRewardRelics = new RelicType[generatedLayout.rooms.size()];
        bonusKnifeAvailableRooms = new boolean[generatedLayout.rooms.size()];
        bonusKnifeSpawnedRooms = new boolean[generatedLayout.rooms.size()];
        bonusKnifeCollectedRooms = new boolean[generatedLayout.rooms.size()];
        merchantPurchasedMasks = new int[generatedLayout.rooms.size()];
        for (GeneratedRoom generatedRoom : generatedLayout.rooms) {
            clearedRooms[generatedRoom.id] = !generatedRoom.type.requiresClear;
        }
        generateRoomRewards(dungeonSeed);
    }

    private void generateRoomRewards(long dungeonSeed) {
        Random random = new Random(dungeonSeed ^ (levelNumber * 31L));
        for (GeneratedRoom generatedRoom : generatedLayout.rooms) {
            if (generatedRoom.type == RoomType.LOOT) {
                bonusKnifeAvailableRooms[generatedRoom.id] = random.nextInt(100) < 40;
            }
            int roll = random.nextInt(100);
            boolean elite = generatedRoom.type == RoomType.ELITE;
            if (roll < 50) {
                roomRewardTypes[generatedRoom.id] =
                    CollectableComponent.Type.DEVIL_COINS;
                roomRewardValues[generatedRoom.id] = elite ? 10 : 5;
            } else if (roll < 80) {
                roomRewardTypes[generatedRoom.id] = CollectableComponent.Type.HEAL;
                roomRewardValues[generatedRoom.id] = elite ? 30 : 20;
            } else if (roll < 92) {
                roomRewardTypes[generatedRoom.id] =
                    CollectableComponent.Type.MAX_HEALTH;
                roomRewardValues[generatedRoom.id] = elite ? 15 : 10;
            } else {
                roomRewardTypes[generatedRoom.id] = CollectableComponent.Type.RELIC;
                roomRewardValues[generatedRoom.id] = 1;
                RelicType[] relicTypes = RelicType.values();
                roomRewardRelics[generatedRoom.id] =
                    relicTypes[random.nextInt(relicTypes.length)];
            }
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
        checkpointReached = levelNumber == 3 ? 1 : levelNumber == MAX_LEVEL ? 2 : 0;
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

    public void acceptLevelUpgrade(RelicType type) {
        if (!levelComplete || bossMode) {
            return;
        }
        type.apply(player);
        Gdx.app.log("LevelComplete", "Storm Boon selected: " + type.displayName);
        if (levelNumber == 3) {
            captureCheckpoint(4, false, 1);
        } else if (levelNumber == MAX_LEVEL) {
            captureCheckpoint(MAX_LEVEL, true, 2);
        }
        if (levelNumber < MAX_LEVEL) {
            startNextLevel();
        } else {
            startBossEncounter();
        }
    }

    private void startNextLevel() {
        removeCurrentProjectiles();
        removeCurrentEnemies();
        removeCurrentCollectables();
        removeCurrentMerchant();
        removeCurrentChest();
        removeCurrentLever();
        removeCurrentKeys();
        removeCurrentEnvironment();
        for (Body body : roomCollisionBodies) {
            world.destroyBody(body);
        }
        roomCollisionBodies.clear();

        levelNumber++;
        levelComplete = false;
        checkpointReached = 0;
        currentRoomIndex = 0;
        generateDungeonLayout();
        room = RoomLoader.load(generatedLayout.getRoom(0).templatePath);
        createRoomCollisionBodies();
        spawnEnvironmentForCurrentRoom();
        resetPlayerForNewLevel();
        spawnEnemiesForCurrentRoom();
        spawnLeverIfAvailable();
        spawnRoomRewardIfAvailable();
        spawnBonusKnifeIfAvailable();
        spawnMerchantIfAvailable();

        Gdx.app.log("DungeonGenerator", "\n" + generatedLayout.toDebugString());
        logCurrentRoom();
        logLevelStart();
    }

    private void startBossEncounter() {
        boolean restarting = bossMode;
        removeCurrentProjectiles();
        removeCurrentEnemies();
        removeCurrentCollectables();
        removeCurrentMerchant();
        removeCurrentChest();
        removeCurrentLever();
        removeCurrentKeys();
        removeCurrentEnvironment();

        bossMode = true;
        bossVictory = false;
        levelComplete = false;
        checkpointReached = 0;

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        playerState.dead = false;
        playerState.controlsLocked = false;
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        playerHealth.current = restarting
            ? playerHealth.maximum
            : Math.min(
                playerHealth.maximum,
                playerHealth.current + playerHealth.maximum * 0.25f
            );
        InvulnerabilityComponent playerInvulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        playerInvulnerability.timeRemaining = 0.75f;
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
        player.getComponent(StatusEffectComponent.class).clear();
        player.getComponent(PlayerRangedComponent.class).resetCooldown();

        Body playerBody = player.getComponent(PhysicsComponent.class).body;
        playerBody.setTransform(room.playerSpawn, 0f);
        playerBody.setLinearVelocity(0f, 0f);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        playerPosition.x = room.playerSpawn.x;
        playerPosition.y = room.playerSpawn.y;

        Vector2 bossSpawn = new Vector2(room.width / 2f, room.height / 2f);
        boss = EnemyFactory.createBoss(world, bossSpawn);
        enemies.add(boss);
        engine.addEntity(boss);
        Gdx.app.log("Boss", "Irhos encounter started: Iron Fist");
    }

    private void updateBossVictory() {
        if (bossVictory || boss == null) {
            return;
        }
        EnemyAIComponent ai = boss.getComponent(EnemyAIComponent.class);
        if (ai.state != EnemyAIComponent.State.DEAD) {
            return;
        }
        bossVictory = true;
        player.getComponent(PlayerComponent.class).controlsLocked = true;
        removeCurrentProjectiles();
        Gdx.app.log("Boss", "Irhos defeated - run complete");
    }

    private int getRoomCountForCurrentLevel() {
        return BASE_ROOM_COUNT + levelNumber;
    }

    private void logLevelStart() {
        Gdx.app.log(
            "Level",
            "Level " + levelNumber + " started with "
                + generatedLayout.rooms.size()
                + " rooms | Theme: " + currentTheme.displayName
                + " | Mechanic: " + currentTheme.mechanic
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
        player.getComponent(StatusEffectComponent.class).clear();
        player.getComponent(PlayerRangedComponent.class).resetCooldown();
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
        removeCurrentChest();
        removeCurrentLever();
        removeCurrentKeys();
        removeCurrentEnvironment();

        for (Body body : roomCollisionBodies) {
            world.destroyBody(body);
        }
        roomCollisionBodies.clear();

        currentRoomIndex = roomIndex;
        room = RoomLoader.load(generatedLayout.getRoom(currentRoomIndex).templatePath);
        logCurrentRoom();
        createRoomCollisionBodies();
        spawnEnvironmentForCurrentRoom();

        Vector2 playerSpawn = room.doorSpawns.get(arrivalDoor);
        Body playerBody = player.getComponent(PhysicsComponent.class).body;
        playerBody.setTransform(playerSpawn, 0f);
        playerBody.setLinearVelocity(0f, 0f);
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        playerPosition.x = playerSpawn.x;
        playerPosition.y = playerSpawn.y;

        spawnEnemiesForCurrentRoom();
        spawnLeverIfAvailable();
        spawnRoomRewardIfAvailable();
        spawnBonusKnifeIfAvailable();
        spawnMerchantIfAvailable();
    }

    private void spawnEnemiesForCurrentRoom() {
        if (clearedRooms[currentRoomIndex]) {
            return;
        }

        RoomType roomType = generatedLayout.getRoom(currentRoomIndex).type;
        int spawnLimit = roomType == RoomType.ELITE ? 2 : room.enemySpawns.size;
        EnemySpawnDefinition.Type[] encounterRecipe = EncounterDirector.createRecipe(
            levelNumber,
            roomType,
            spawnLimit,
            dungeonSeed ^ (currentRoomIndex * 1_000_003L)
        );
        Gdx.app.log(
            "Encounter",
            "Level " + levelNumber + " room " + currentRoomIndex
                + " composition: " + java.util.Arrays.toString(encounterRecipe)
        );
        for (int index = 0; index < spawnLimit; index++) {
            EnemySpawnDefinition spawn = room.enemySpawns.get(index);
            EnemySpawnDefinition.Type enemyType = encounterRecipe[index];
            Entity enemy;
            if (enemyType == EnemySpawnDefinition.Type.RANGED) {
                enemy = EnemyFactory.createRanged(world, spawn.position);
            } else if (enemyType == EnemySpawnDefinition.Type.HEAVY) {
                enemy = EnemyFactory.createHeavy(world, spawn.position);
            } else if (enemyType == EnemySpawnDefinition.Type.CHARGER) {
                enemy = EnemyFactory.createCharger(world, spawn.position);
            } else if (enemyType == EnemySpawnDefinition.Type.NECROMANCER) {
                enemy = EnemyFactory.createNecromancer(world, spawn.position);
            } else if (enemyType == EnemySpawnDefinition.Type.SHIELD_GUARD) {
                enemy = EnemyFactory.createShieldGuard(world, spawn.position);
            } else {
                enemy = EnemyFactory.createMelee(world, spawn.position);
            }
            if (enemyType == EnemySpawnDefinition.Type.RANGED) {
                long dropSeed = dungeonSeed
                    ^ (levelNumber * 1_000_003L)
                    ^ (currentRoomIndex * 97_409L)
                    ^ (index * 31_337L);
                if (new Random(dropSeed).nextInt(100) < 25) {
                    enemy.add(new KnifeDropComponent());
                }
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
            if (enemy.getComponent(ChargerComponent.class) != null) {
                health.maximum *= 1.25f;
            }
            if (enemy.getComponent(ShieldGuardComponent.class) != null) {
                health.maximum *= 1.4f;
                EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
                ai.movementSpeed *= 0.85f;
                ai.attackDamage *= 1.1f;
            }
            if (roomType == RoomType.ELITE && index == spawnLimit - 1) {
                enemy.add(new KeyCarrierComponent());
            }
            boolean resurrectionTheme =
                currentTheme.mechanic == LevelTheme.Mechanic.RESURRECTION
                    || currentTheme.mechanic == LevelTheme.Mechanic.COMBINED;
            if (resurrectionTheme
                && enemyType != EnemySpawnDefinition.Type.NECROMANCER) {
                enemy.add(new ResurrectionComponent());
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

    public void restoreLatestCheckpoint() {
        removeCurrentProjectiles();
        removeCurrentEnemies();
        removeCurrentCollectables();
        removeCurrentMerchant();
        removeCurrentChest();
        removeCurrentLever();
        removeCurrentKeys();
        removeCurrentEnvironment();
        for (Body body : roomCollisionBodies) {
            world.destroyBody(body);
        }
        roomCollisionBodies.clear();

        boss = null;
        bossMode = false;
        bossVictory = false;
        levelComplete = false;
        checkpointReached = 0;
        levelNumber = checkpoint.restartLevel;

        HealthComponent health = player.getComponent(HealthComponent.class);
        health.maximum = checkpoint.maximumHealth;
        health.current = health.maximum;
        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        inventory.devilCoins = checkpoint.devilCoins;
        inventory.enemiesDefeated = checkpoint.enemiesDefeated;
        RelicInventoryComponent relics =
            player.getComponent(RelicInventoryComponent.class);
        relics.ironHeart = checkpoint.ironHeart;
        relics.stormEdge = checkpoint.stormEdge;
        relics.windstepSigil = checkpoint.windstepSigil;
        player.getComponent(AttackComponent.class).damage = checkpoint.attackDamage;
        player.getComponent(DashComponent.class).cooldownDuration = checkpoint.dashCooldown;
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        knives.maximumCharges = checkpoint.knifeCapacity;
        knives.charges = checkpoint.knives;

        if (checkpoint.bossCheckpoint) {
            currentRoomIndex = 0;
            generateDungeonLayout();
            room = RoomLoader.load(generatedLayout.getRoom(0).templatePath);
            createRoomCollisionBodies();
            startBossEncounter();
            Gdx.app.log("Checkpoint", "Final checkpoint restored. Boss encounter restarted.");
            return;
        }

        currentRoomIndex = 0;
        generateDungeonLayout();
        room = RoomLoader.load(generatedLayout.getRoom(0).templatePath);
        createRoomCollisionBodies();
        spawnEnvironmentForCurrentRoom();
        resetPlayerAfterCheckpoint();
        spawnEnemiesForCurrentRoom();
        spawnLeverIfAvailable();
        spawnRoomRewardIfAvailable();
        spawnBonusKnifeIfAvailable();
        spawnMerchantIfAvailable();

        Gdx.app.log("DungeonGenerator", "\n" + generatedLayout.toDebugString());
        logCurrentRoom();
        logLevelStart();
        Gdx.app.log("Checkpoint", "Restored at the beginning of Level " + levelNumber);
    }

    private void captureCheckpoint(int restartLevel, boolean bossCheckpoint, int number) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        RunInventoryComponent inventory = player.getComponent(RunInventoryComponent.class);
        RelicInventoryComponent relics =
            player.getComponent(RelicInventoryComponent.class);
        AttackComponent attack = player.getComponent(AttackComponent.class);
        DashComponent dash = player.getComponent(DashComponent.class);
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        health.current = health.maximum;
        checkpoint.capture(
            restartLevel,
            bossCheckpoint,
            health.maximum,
            inventory.devilCoins,
            inventory.enemiesDefeated,
            relics.ironHeart,
            relics.stormEdge,
            relics.windstepSigil,
            attack.damage,
            dash.cooldownDuration,
            knives.charges,
            knives.maximumCharges
        );
        checkpointReached = number;
        if (number > 0) {
            GamePreferences.saveCheckpoint(checkpoint);
            Gdx.app.log(
                "Checkpoint",
                "Checkpoint " + number + " captured | HP fully restored | Coins: "
                    + inventory.devilCoins
            );
        }
    }

    /** Keeps Continue available when a running game returns through the pause menu. */
    private void returnToMainMenu() {
        GamePreferences.saveCheckpoint(checkpoint);
        game.showMainMenu();
    }

    private void resetPlayerAfterCheckpoint() {
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
        player.getComponent(StatusEffectComponent.class).clear();
        player.getComponent(PlayerRangedComponent.class).resetCooldown();
        physics.body.setTransform(room.playerSpawn, 0f);
        physics.body.setLinearVelocity(0f, 0f);

        PositionComponent position = player.getComponent(PositionComponent.class);
        position.x = room.playerSpawn.x;
        position.y = room.playerSpawn.y;

        Gdx.app.log("Player", "Player state restored from checkpoint.");
    }

    private boolean areAllEnemiesDead() {
        if (enemies.size == 0) {
            return true;
        }
        for (Entity enemy : enemies) {
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            ResurrectionComponent resurrection =
                enemy.getComponent(ResurrectionComponent.class);
            if (resurrection != null && resurrection.awaitingResurrection) {
                return false;
            }
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

        CollectableComponent.Type rewardType = roomRewardTypes[currentRoomIndex];
        int value = roomRewardValues[currentRoomIndex];
        RelicType relicType = roomRewardRelics[currentRoomIndex];
        Vector2 spawn = room.lootSpawns.first();
        if (chestOpenedRooms[currentRoomIndex]) {
            Entity collectable = CollectableFactory.createReward(
                spawn,
                rewardType,
                value,
                relicType
            );
            collectables.add(collectable);
            engine.addEntity(collectable);
        } else {
            chest = ChestFactory.create(spawn, rewardType, value, relicType);
            chest.getComponent(ChestComponent.class).unlocked =
                isCurrentChestUnlocked();
            engine.addEntity(chest);
            Gdx.app.log("Chest", "Approach the chest and press E after it unlocks");
        }
        rewardSpawnedRooms[currentRoomIndex] = true;
    }

    private void spawnBonusKnifeIfAvailable() {
        if (generatedLayout.getRoom(currentRoomIndex).type != RoomType.LOOT
            || !bonusKnifeAvailableRooms[currentRoomIndex]
            || bonusKnifeSpawnedRooms[currentRoomIndex]
            || bonusKnifeCollectedRooms[currentRoomIndex]) {
            return;
        }
        Vector2 lootPosition = room.lootSpawns.first();
        Entity knife = CollectableFactory.createKnife(
            new Vector2(lootPosition.x + 0.65f, lootPosition.y),
            true
        );
        collectables.add(knife);
        engine.addEntity(knife);
        bonusKnifeSpawnedRooms[currentRoomIndex] = true;
        Gdx.app.log("Loot", "This loot room contains a bonus knife");
    }

    private void spawnDroppedKnives() {
        for (Entity enemy : enemies) {
            KnifeDropComponent drop = enemy.getComponent(KnifeDropComponent.class);
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (drop == null || drop.dropped || ai.state != EnemyAIComponent.State.DEAD) {
                continue;
            }
            ResurrectionComponent resurrection =
                enemy.getComponent(ResurrectionComponent.class);
            if (resurrection != null && resurrection.awaitingResurrection) {
                continue;
            }
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            Entity knife = CollectableFactory.createKnife(
                new Vector2(position.x, position.y),
                false
            );
            collectables.add(knife);
            engine.addEntity(knife);
            drop.dropped = true;
            Gdx.app.log("Loot", "A ranged enemy dropped a knife");
        }
    }

    private void updateChestState() {
        if (chest == null) {
            return;
        }
        ChestComponent data = chest.getComponent(ChestComponent.class);
        data.unlocked = isCurrentChestUnlocked();
        if (data.opened) {
            chestOpenedRooms[currentRoomIndex] = true;
        }
    }

    private boolean isCurrentChestUnlocked() {
        RoomType type = generatedLayout.getRoom(currentRoomIndex).type;
        return type == RoomType.LOOT
            ? leverActivatedRooms[currentRoomIndex]
            : type == RoomType.ELITE
                ? keyCollectedRooms[currentRoomIndex]
                : clearedRooms[currentRoomIndex];
    }

    private void spawnLeverIfAvailable() {
        if (generatedLayout.getRoom(currentRoomIndex).type != RoomType.LOOT
            || leverActivatedRooms[currentRoomIndex]) {
            return;
        }
        lever = LeverFactory.create(room.merchantSpawn);
        engine.addEntity(lever);
        Gdx.app.log("Lever", "Approach the blue lever and press E");
    }

    private void updateLeverState() {
        if (lever == null) {
            return;
        }
        LeverComponent data = lever.getComponent(LeverComponent.class);
        if (data.activated) {
            leverActivatedRooms[currentRoomIndex] = true;
        }
    }

    private void spawnDroppedChestKey() {
        if (generatedLayout.getRoom(currentRoomIndex).type != RoomType.ELITE
            || keyDroppedRooms[currentRoomIndex]) {
            return;
        }
        for (Entity enemy : enemies) {
            KeyCarrierComponent carrier = enemy.getComponent(KeyCarrierComponent.class);
            EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
            if (carrier == null || ai.state != EnemyAIComponent.State.DEAD) {
                continue;
            }
            carrier.keyDropped = true;
            keyDroppedRooms[currentRoomIndex] = true;
            PositionComponent position = enemy.getComponent(PositionComponent.class);
            spawnChestKey(new Vector2(position.x, position.y));
            Gdx.app.log("Key", "The elite key-carrier dropped a chest key");
            return;
        }
    }

    private void spawnRoomKeyIfAvailable() {
        if (!keyDroppedRooms[currentRoomIndex]
            || keyCollectedRooms[currentRoomIndex]
            || keySpawnedRooms[currentRoomIndex]) {
            return;
        }
        spawnChestKey(room.lootSpawns.first());
    }

    private void spawnChestKey(Vector2 position) {
        Entity key = KeyFactory.create(position);
        chestKeys.add(key);
        engine.addEntity(key);
        keySpawnedRooms[currentRoomIndex] = true;
    }

    private void updateKeyState() {
        for (int index = chestKeys.size - 1; index >= 0; index--) {
            Entity key = chestKeys.get(index);
            if (!key.getComponent(ChestKeyComponent.class).collected) {
                continue;
            }
            keyCollectedRooms[currentRoomIndex] = true;
            keySpawnedRooms[currentRoomIndex] = false;
            engine.removeEntity(key);
            chestKeys.removeIndex(index);
        }
    }

    private void updateCollectedRewards() {
        for (int index = collectables.size - 1; index >= 0; index--) {
            Entity collectable = collectables.get(index);
            CollectableComponent data =
                collectable.getComponent(CollectableComponent.class);
            if (!data.collected) {
                continue;
            }
            if (data.roomReward) {
                rewardCollectedRooms[currentRoomIndex] = true;
            }
            if (data.bonusKnife) {
                bonusKnifeCollectedRooms[currentRoomIndex] = true;
                bonusKnifeSpawnedRooms[currentRoomIndex] = false;
            }
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
        if (!bonusKnifeCollectedRooms[currentRoomIndex]) {
            bonusKnifeSpawnedRooms[currentRoomIndex] = false;
        }
    }

    private void removeCurrentChest() {
        if (chest == null) {
            return;
        }
        engine.removeEntity(chest);
        chest = null;
        if (!rewardCollectedRooms[currentRoomIndex]) {
            rewardSpawnedRooms[currentRoomIndex] = false;
        }
    }

    private void removeCurrentLever() {
        if (lever == null) {
            return;
        }
        engine.removeEntity(lever);
        lever = null;
    }

    private void removeCurrentKeys() {
        for (Entity key : chestKeys) {
            engine.removeEntity(key);
        }
        chestKeys.clear();
        if (!keyCollectedRooms[currentRoomIndex]) {
            keySpawnedRooms[currentRoomIndex] = false;
        }
    }

    private void spawnMerchantIfAvailable() {
        RoomType type = generatedLayout.getRoom(currentRoomIndex).type;
        if (type != RoomType.MERCHANT) {
            return;
        }

        merchant = MerchantFactory.createRelicMerchant(
            room.merchantSpawn,
            levelNumber,
            dungeonSeed ^ (currentRoomIndex * 97_409L)
        );
        merchant.getComponent(MerchantComponent.class).purchasedMask =
            merchantPurchasedMasks[currentRoomIndex];
        engine.addEntity(merchant);
        Gdx.app.log(
            "Merchant",
            "Approach the purple merchant. Press 1-5 to buy knives, a pouch, or relics."
        );
    }

    private boolean isPlayerNearMerchant() {
        if (merchant == null) {
            return false;
        }
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        PositionComponent merchantPosition = merchant.getComponent(PositionComponent.class);
        float deltaX = playerPosition.x - merchantPosition.x;
        float deltaY = playerPosition.y - merchantPosition.y;
        return deltaX * deltaX + deltaY * deltaY <= 1.4f * 1.4f;
    }

    private void updateMerchantState() {
        if (merchant == null) {
            return;
        }
        MerchantComponent merchantData = merchant.getComponent(MerchantComponent.class);
        merchantPurchasedMasks[currentRoomIndex] = merchantData.purchasedMask;
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
        float red = currentTheme.red;
        float green = currentTheme.green;
        float blue = currentTheme.blue;
        switch (type) {
            case START:
                green += 0.06f;
                blue += 0.06f;
                break;
            case COMBAT:
                break;
            case LOOT:
                red += 0.07f;
                green += 0.06f;
                break;
            case MERCHANT:
                red += 0.03f;
                blue += 0.08f;
                break;
            case ELITE:
                red += 0.09f;
                green *= 0.65f;
                break;
            case EXIT:
                blue += 0.07f;
                break;
        }
        shapeRenderer.setColor(
            Math.min(1f, red),
            Math.min(1f, green),
            Math.min(1f, blue),
            1f
        );
    }

    private void drawDarknessOverlay(PositionComponent playerPosition) {
        boolean darknessEnabled = currentTheme.mechanic == LevelTheme.Mechanic.DARKNESS
            || currentTheme.mechanic == LevelTheme.Mechanic.COMBINED;
        if (!darknessEnabled || bossMode) {
            return;
        }

        float visionRadius = 3.5f;
        float visionLeft = Math.max(0f, playerPosition.x - visionRadius);
        float visionRight = Math.min(room.width, playerPosition.x + visionRadius);
        float visionBottom = Math.max(0f, playerPosition.y - visionRadius);
        float visionTop = Math.min(room.height, playerPosition.y + visionRadius);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setColor(0.005f, 0.005f, 0.018f, 0.96f);
        shapeRenderer.rect(0f, 0f, visionLeft, room.height);
        shapeRenderer.rect(
            visionRight,
            0f,
            Math.max(0f, room.width - visionRight),
            room.height
        );
        shapeRenderer.rect(
            visionLeft,
            0f,
            Math.max(0f, visionRight - visionLeft),
            visionBottom
        );
        shapeRenderer.rect(
            visionLeft,
            visionTop,
            Math.max(0f, visionRight - visionLeft),
            Math.max(0f, room.height - visionTop)
        );
    }

    private void drawEnemy(
        PositionComponent position,
        EnemyAIComponent ai,
        HealthComponent health,
        InvulnerabilityComponent invulnerability,
        boolean ranged,
        boolean heavy,
        BossComponent bossData,
        ChargerComponent charger,
        ResurrectionComponent resurrection,
        NecromancerComponent necromancer,
        ShieldGuardComponent shield
    ) {
        float radius = bossData != null
                ? 0.8f
            : heavy
                ? 0.65f
                : charger != null || shield != null ? 0.5f : 0.45f;
        if (resurrection != null && resurrection.awaitingResurrection) {
            shapeRenderer.setColor(0.55f, 0.1f, 0.75f, 1f);
            shapeRenderer.circle(position.x, position.y, radius);
            drawHealthBar(position, health);
            return;
        }
        if (invulnerability.isActive()) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(position.x, position.y, radius);
            drawHealthBar(position, health);
            return;
        }

        if (bossData != null && ai.state != EnemyAIComponent.State.DEAD) {
            setBossColor(bossData);
        } else if (shield != null && ai.state != EnemyAIComponent.State.DEAD) {
            shapeRenderer.setColor(
                shield.isGuardBroken() ? 0.2f : 0.08f,
                shield.isGuardBroken() ? 0.45f : 0.3f,
                shield.isGuardBroken() ? 1f : 0.65f,
                1f
            );
        } else if (necromancer != null && ai.state != EnemyAIComponent.State.DEAD) {
            shapeRenderer.setColor(0.85f, 0.25f, 1f, 1f);
        } else if (charger != null && ai.state != EnemyAIComponent.State.DEAD) {
            shapeRenderer.setColor(0.75f, 0.9f, 0.08f, 1f);
        } else if (heavy && ai.state != EnemyAIComponent.State.DEAD) {
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

    private void drawBossAttackTelegraph(BossComponent bossData) {
        if ((bossData.phase == BossComponent.Phase.DEVILS_CROWN
                || bossData.phase == BossComponent.Phase.IRHOS_REVEALED)
            && bossData.attackState == BossComponent.AttackState.CROWN_WINDUP) {
            float progress = 1f - Math.max(
                0f,
                bossData.attackTimeRemaining / bossData.telegraphDuration
            );
            float step = (float) (Math.PI * 2.0 / 12.0);
            shapeRenderer.setColor(0.75f, 0.08f, 1f, 0.25f + progress * 0.25f);
            for (int index = 0; index < 12; index++) {
                if (index == bossData.crownSafeGap
                    || index == (bossData.crownSafeGap + 1) % 12) {
                    continue;
                }
                float angle = bossData.crownRotation + index * step;
                float directionX = (float) Math.cos(angle);
                float directionY = (float) Math.sin(angle);
                shapeRenderer.circle(
                    bossData.crownOriginX + directionX * 1.35f,
                    bossData.crownOriginY + directionY * 1.35f,
                    0.24f + progress * 0.1f
                );
            }
            return;
        }
        if ((bossData.phase == BossComponent.Phase.BURNING_GAUNTLETS
                || bossData.phase == BossComponent.Phase.IRHOS_REVEALED)
            && bossData.attackState == BossComponent.AttackState.FLAME_PUNCH_WINDUP) {
            float progress = 1f - Math.max(
                0f,
                bossData.attackTimeRemaining / bossData.telegraphDuration
            );
            shapeRenderer.setColor(1f, 0.18f + progress * 0.4f, 0.02f, 0.28f);
            for (float distance = 0.7f;
                 distance <= bossData.punchTelegraphLength;
                 distance += 0.55f) {
                shapeRenderer.circle(
                    bossData.punchOriginX + bossData.punchDirectionX * distance,
                    bossData.punchOriginY + bossData.punchDirectionY * distance,
                    0.42f
                );
            }
            return;
        }
        if ((bossData.phase != BossComponent.Phase.IRON_FIST
                && bossData.phase != BossComponent.Phase.IRHOS_REVEALED)
            || (bossData.attackState != BossComponent.AttackState.SLAM_WINDUP
            && bossData.attackState != BossComponent.AttackState.SLAM_RECOVERY)) {
            return;
        }
        if (bossData.attackState == BossComponent.AttackState.SLAM_WINDUP) {
            float progress = 1f - Math.max(
                0f,
                bossData.attackTimeRemaining / bossData.telegraphDuration
            );
            shapeRenderer.setColor(1f, 0.45f + progress * 0.25f, 0.05f, 0.22f);
        } else {
            shapeRenderer.setColor(0.55f, 0.08f, 0.02f, 0.16f);
        }
        shapeRenderer.circle(
            bossData.slamTargetX,
            bossData.slamTargetY,
            bossData.slamRadius
        );
    }

    private void setBossColor(BossComponent bossData) {
        switch (bossData.phase) {
            case IRON_FIST:
                shapeRenderer.setColor(0.45f, 0.48f, 0.55f, 1f);
                break;
            case BURNING_GAUNTLETS:
                shapeRenderer.setColor(1f, 0.28f, 0.03f, 1f);
                break;
            case DEVILS_CROWN:
                shapeRenderer.setColor(0.65f, 0.12f, 0.85f, 1f);
                break;
            case IRHOS_REVEALED:
                shapeRenderer.setColor(0.95f, 0.08f, 0.12f, 1f);
                break;
        }
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

    @Override public void show() { Gdx.input.setInputProcessor(hud.getStage()); }
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == hud.getStage()) {
            Gdx.input.setInputProcessor(null);
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
}
