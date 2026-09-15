package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import java.io.File;
import java.lang.reflect.Proxy;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Executable regression assertions using native Box2D and production ECS systems. */
public final class WitchRegressionTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        // Gameplay systems only need logging; textures are deliberately not loaded.
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
            new Class<?>[] {Application.class}, (proxy, method, values) -> null);
        verifyEscapeAndBoundaries();
        verifyDetectionAndWallRecovery();
        verifyCast(false, false, 1f / 120f);
        verifyCast(true, false, 1f / 120f);
        verifyCast(false, true, 1f / 120f);
        verifyCast(false, false, 0.2f);
        verifyCast(false, false, 1f);
        verifyChargeAndReactions();
        verifyEncounters();
        System.out.println("Witch regressions passed: " + checks + " assertions");
    }

    private static void verifyEscapeAndBoundaries() throws Exception {
        String[] maps = {"room_normal", "room_water", "room_poison", "room_spike",
            "room_fire", "room_exit"};
        // Reproduce the old bottom doorway escape with the existing bullet body.
        try (Simulation sim = new Simulation(false, false)) {
            loadWalls(sim.world, maps[0]);
            sim.placeWitch(10f, 2.5f);
            sim.moveFor(0f, -WitchComponent.CHARGE_SPEED, 1f / 60f, 90);
            require(sim.position().y < 0f, "old open bottom doorway reproduces escape");
        }
        float[][] directions = {{10f, 2.5f, 0f, -1f}, {10f, 9.5f, 0f, 1f},
            {2.5f, 6f, -1f, 0f}, {17.5f, 6f, 1f, 0f},
            {3f, 3f, -0.70710677f, -0.70710677f}};
        for (String map : maps) {
            for (float[] direction : directions) {
                for (float delta : new float[] {1f / 120f, 1f / 60f, 0.05f, 0.2f}) {
                    try (Simulation sim = new Simulation(false, true)) {
                        loadWalls(sim.world, map);
                        sim.placeWitch(direction[0], direction[1]);
                        sim.moveFor(direction[2] * WitchComponent.CHARGE_SPEED,
                            direction[3] * WitchComponent.CHARGE_SPEED, delta,
                            (int) Math.ceil(2f / delta));
                        requireInside(sim.position(), map + " charge direction");
                    }
                }
            }
        }
        try (Simulation sim = new Simulation(false, true)) {
            WorldUtils.createStaticRectangle(sim.world, new Rectangle(8f, 3f, 0.1f, 6f));
            sim.placeWitch(6f, 6f);
            sim.moveFor(WitchComponent.CHARGE_SPEED, 0f, 1f / 60f, 90);
            require(sim.position().x <= 8f - WitchComponent.BODY_RADIUS + 0.02f,
                "internal thin wall blocks charge");
        }
        try (WorldOwner owner = new WorldOwner()) {
            WorldUtils.createWitchRoomBounds(owner.world, 20f, 12f);
            Body player = WorldUtils.createDynamicCircle(owner.world, 10f, 1f, 0.4f);
            player.setLinearVelocity(0f, -3f);
            for (int i = 0; i < 60; i++) owner.world.step(1f / 60f, 6, 2);
            require(player.getPosition().y < 0f, "witch perimeter preserves player doorway travel");
        }
    }

    private static void verifyDetectionAndWallRecovery() {
        try (Simulation sim = new Simulation(true, true)) {
            sim.placeWitch(4f, 6f);
            sim.placePlayer(16f, 6f);
            sim.tick(0.1f);
            sim.tick(0.1f);
            require(sim.witch().state == WitchComponent.State.IDLE, "outside awareness idles");
            require(sim.position().x == 4f, "unaware witch does not move");
            require(sim.animation().state == AnimationComponent.State.IDLE, "unaware visual idle");
            require(sim.animation().idle.getPlayMode() == Animation.PlayMode.LOOP, "idle loops");
            sim.placePlayer(10f, 6f);
            sim.tick(1f / 60f);
            require(sim.witch().state == WitchComponent.State.CHASE, "detected far player chases");
            require(sim.animation().state == AnimationComponent.State.WALK, "moving visual run");
            sim.placePlayer(sim.position().x + 1f, sim.position().y + 2f);
            sim.tick(1f / 60f);
            require(sim.witch().state == WitchComponent.State.CHARGE_WINDUP,
                "close vertically offset player triggers charge");
        }
        float[][] cases = {{10f, 0.5f, 0f, -1f}, {10f, 11.5f, 0f, 1f},
            {0.5f, 6f, -1f, 0f}, {19.5f, 6f, 1f, 0f}};
        for (float[] c : cases) {
            try (Simulation sim = new Simulation(true, true)) {
                sim.placeWitch(c[0], c[1]);
                sim.placePlayer(c[0] - c[2] * 2f, c[1] - c[3] * 2f);
                sim.witch().state = WitchComponent.State.CHARGING;
                sim.witch().chargeDirectionX = c[2];
                sim.witch().chargeDirectionY = c[3];
                for (int i = 0; i < 20 && sim.witch().state == WitchComponent.State.CHARGING; i++) {
                    sim.tick(1f / 60f);
                }
                require(sim.witch().state == WitchComponent.State.CHARGE_RECOVERY,
                    "static wall contact enters early recovery");
                require(sim.witch().stateTime < sim.witch().chargeDuration,
                    "wall recovery precedes charge timeout");
                require(sim.animation().state == AnimationComponent.State.IDLE, "charge recovery idle");
                requireInside(sim.position(), "early wall recovery");
            }
        }
    }

    private static void verifyCast(boolean miss, boolean invulnerable, float delta) {
        try (Simulation sim = new Simulation(true, true)) {
            sim.placeWitch(4f, 6f);
            sim.placePlayer(7f, 6f);
            sim.witch().chargeCooldownRemaining = 100f;
            sim.tick(delta);
            require(sim.witch().state == WitchComponent.State.SIDE_ATTACK_WINDUP,
                "side magic takes priority over close charge");
            require(sim.animation().stateTime == 0f, "cast begins on frame zero");
            boolean sawFinalFrame = false;
            int damageEvents = 0;
            while (sim.witch().state != WitchComponent.State.SIDE_ATTACK_RECOVERY) {
                int frame = sim.witch().getSideAttackAnimationFrame();
                if (miss && frame >= 6 && !sim.witch().sideAttackDamageChecked) {
                    sim.placePlayer(7f, 8f);
                }
                sim.player.getComponent(InvulnerabilityComponent.class).timeRemaining =
                    invulnerable && !sim.witch().sideAttackDamageChecked ? 1f : 0f;
                float before = sim.health();
                sim.tick(delta);
                if (sim.health() < before) {
                    damageEvents++;
                    require(sim.witch().getSideAttackAnimationFrame() == 7,
                        "damage occurs at frame seven");
                    require(sim.animation().attack.getKeyFrameIndex(sim.animation().stateTime) == 7,
                        "rendered cast frame matches damage frame");
                }
                if (!sim.witch().sideAttackDamageChecked) require(sim.health() == 100f,
                    "frames zero through six cannot damage");
                if (sim.witch().sideAttackDamageChecked && miss) sim.placePlayer(7f, 6f);
                if (sim.animation().state == AnimationComponent.State.ATTACK
                    && sim.animation().attack.getKeyFrameIndex(sim.animation().stateTime) == 8) {
                    sawFinalFrame = true;
                }
            }
            require(sawFinalFrame, "cast displays final frame before recovery");
            require(damageEvents == (miss || invulnerable ? 0 : 1), "one sampled opportunity per cast");
            require(sim.witch().sideAttackDamageChecked, "miss/invulnerability consumes opportunity");
            require(sim.animation().state == AnimationComponent.State.IDLE, "side recovery selects idle");
            float idleTime = sim.animation().stateTime;
            sim.tick(0.1f);
            require(sim.animation().stateTime > idleTime, "recovery idle keeps advancing");
            sim.placePlayer(7f, 6f);
            while (sim.witch().state == WitchComponent.State.SIDE_ATTACK_RECOVERY) sim.tick(0.01f);
            sim.witch().sideAttackCooldownRemaining = 0f;
            sim.tick(0.01f);
            require(sim.witch().state == WitchComponent.State.SIDE_ATTACK_WINDUP, "new cast begins");
            require(sim.animation().stateTime == 0f && !sim.witch().sideAttackDamageChecked,
                "new cast resets animation and damage opportunity");
        }
    }

    private static void verifyChargeAndReactions() {
        try (Simulation sim = new Simulation(true, true)) {
            sim.placeWitch(4f, 6f);
            sim.placePlayer(4.6f, 6f);
            sim.witch().state = WitchComponent.State.CHARGING;
            sim.witch().chargeDirectionX = 1f;
            sim.witch().chargeDirectionY = 0f;
            sim.witch().facingLeft = false;
            sim.player.getComponent(InvulnerabilityComponent.class).timeRemaining = 1f;
            sim.tick(1f / 120f);
            require(sim.health() == 100f && !sim.witch().chargeHitPlayer,
                "charge respects invulnerability without consuming a hit");
            for (int i = 0; i < 20; i++) {
                sim.player.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
                sim.tick(1f / 120f);
            }
            require(sim.health() == 100f - sim.ai().attackDamage * sim.witch().chargeDamageMultiplier,
                "charge contact damages once despite expiring invulnerability");
            sim.placePlayer(sim.position().x - 1f, 6f);
            sim.tick(1f / 120f);
            require(sim.velocity().vx > 0f && !sim.witch().facingLeft,
                "charge direction and facing stay locked when player runs past");
            sim.witchEntity.getComponent(HealthComponent.class).current -= 1f;
            sim.tick(1f / 120f);
            require(sim.animation().state == AnimationComponent.State.HURT, "hurt visual appears");
            require(sim.witch().state == WitchComponent.State.CHARGING, "hurt does not rewrite AI");
            sim.witchEntity.getComponent(HealthComponent.class).current = 0f;
            sim.tick(1f / 120f);
            require(sim.animation().state == AnimationComponent.State.DEAD, "death overrides hurt");
            boolean[] frames = new boolean[10];
            for (int i = 0; i < 150; i++) {
                sim.tick(1f / 120f);
                frames[sim.animation().death.getKeyFrameIndex(sim.animation().stateTime)] = true;
            }
            for (boolean frame : frames) require(frame, "death displays every frame");
            require(sim.animation().death.getKeyFrameIndex(sim.animation().stateTime) == 9,
                "death holds final frame without looping");
        }
    }

    private static void verifyEncounters() {
        boolean sawWitch = false;
        for (int level = 1; level <= 10; level++) {
            for (RoomType type : RoomType.values()) {
                for (int seed = 0; seed < 50; seed++) {
                    for (EnemySpawnDefinition.Type enemy : EncounterDirector.createRecipe(level, type, 5, seed)) {
                        require(enemy != EnemySpawnDefinition.Type.CHARGER, "recipes never select chargers");
                        sawWitch |= enemy == EnemySpawnDefinition.Type.WITCH;
                    }
                }
            }
        }
        require(sawWitch, "normal recipes include witches");
    }

    private static void loadWalls(World world, String mapName) throws Exception {
        Element map = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new File("assets/maps/" + mapName + ".tmx")).getDocumentElement();
        float pixelHeight = Float.parseFloat(map.getAttribute("height"))
            * Float.parseFloat(map.getAttribute("tileheight"));
        NodeList objects = map.getElementsByTagName("object");
        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            if (!object.getAttribute("name").startsWith("wall_")) continue;
            float x = Float.parseFloat(object.getAttribute("x"));
            float y = Float.parseFloat(object.getAttribute("y"));
            float width = Float.parseFloat(object.getAttribute("width"));
            float height = Float.parseFloat(object.getAttribute("height"));
            // TmxMapLoader's default Y flip, then RoomLoader's PPM conversion.
            WorldUtils.createStaticRectangle(world, new Rectangle(x / Constants.PPM,
                (pixelHeight - y - height) / Constants.PPM, width / Constants.PPM, height / Constants.PPM));
        }
    }

    private static void requireInside(Vector2 p, String label) {
        float radius = WitchComponent.BODY_RADIUS - 0.02f; // Box2D contact slop.
        require(p.x >= radius && p.x <= 20f - radius && p.y >= radius && p.y <= 12f - radius, label);
    }

    private static void require(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }

    private static class WorldOwner implements AutoCloseable {
        final World world = new World(new Vector2(), true);
        public void close() { world.dispose(); }
    }

    private static final class Simulation extends WorldOwner {
        final Engine engine = new Engine();
        final Entity witchEntity = new Entity();
        final Entity player = new Entity();
        final boolean hasBounds;

        Simulation(boolean aiEnabled, boolean bounds) {
            hasBounds = bounds;
            if (bounds) WorldUtils.createWitchRoomBounds(world, 20f, 12f);
            Body body = WorldUtils.createDynamicCircle(world, 4f, 6f, WitchComponent.BODY_RADIUS);
            WorldUtils.configureWitchBody(body);
            witchEntity.add(new WitchComponent()).add(new EnemyComponent()).add(new EnemyAIComponent())
                .add(new PositionComponent(4f, 6f)).add(new VelocityComponent())
                .add(new PhysicsComponent(body)).add(new HealthComponent(50f));
            ai().detectionRange = WitchComponent.DETECTION_RANGE;
            AnimationComponent visual = new AnimationComponent();
            visual.idle = animation(6, 0.12f, true);
            visual.walk = animation(8, 0.09f, true);
            visual.charge = animation(5, WitchComponent.CHARGE_ANIMATION_FRAME_DURATION, true);
            visual.attack = animation(9, WitchComponent.ATTACK_ANIMATION_FRAME_DURATION, false);
            visual.hurt = animation(3, 0.08f, false);
            visual.death = animation(10, 0.1f, false);
            visual.previousHealth = 50f;
            witchEntity.add(visual);
            player.add(new PlayerComponent()).add(new PositionComponent(-100f, -100f))
                .add(new VelocityComponent()).add(new HealthComponent(100f))
                .add(new InvulnerabilityComponent()).add(new DefenseComponent())
                .add(new FacingComponent()).add(new RunInventoryComponent())
                .add(new PhysicsComponent(WorldUtils.createDynamicCircle(world, -100f, -100f, 0.4f)));
            engine.addEntity(witchEntity);
            engine.addEntity(player);
            if (aiEnabled) engine.addSystem(new WitchSystem(player));
            engine.addSystem(new PhysicsSystem(world));
            if (aiEnabled) {
                engine.addSystem(new EnemyAttackSystem(player, () -> 1));
                engine.addSystem(new DeathSystem(player));
                engine.addSystem(new EnemyAnimationSystem());
            }
        }

        void tick(float delta) { engine.update(delta); }
        WitchComponent witch() { return witchEntity.getComponent(WitchComponent.class); }
        EnemyAIComponent ai() { return witchEntity.getComponent(EnemyAIComponent.class); }
        AnimationComponent animation() { return witchEntity.getComponent(AnimationComponent.class); }
        VelocityComponent velocity() { return witchEntity.getComponent(VelocityComponent.class); }
        Vector2 position() { return witchEntity.getComponent(PhysicsComponent.class).body.getPosition(); }
        float health() { return player.getComponent(HealthComponent.class).current; }
        void placeWitch(float x, float y) { place(witchEntity, x, y); }
        void placePlayer(float x, float y) { place(player, x, y); }
        void moveFor(float vx, float vy, float delta, int ticks) {
            for (int i = 0; i < ticks; i++) {
                velocity().vx = vx;
                velocity().vy = vy;
                tick(delta);
                if (hasBounds) requireInside(position(), "witch stays inside at every physics update");
            }
        }
        private void place(Entity entity, float x, float y) {
            entity.getComponent(PhysicsComponent.class).body.setTransform(x, y, 0f);
            PositionComponent p = entity.getComponent(PositionComponent.class);
            p.x = x;
            p.y = y;
        }
        private static Animation<TextureRegion> animation(int count, float duration, boolean loop) {
            TextureRegion[] frames = new TextureRegion[count];
            for (int i = 0; i < count; i++) frames[i] = new TextureRegion();
            Animation<TextureRegion> animation = new Animation<>(duration, frames);
            animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
            return animation;
        }
    }
}
