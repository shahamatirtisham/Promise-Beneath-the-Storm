package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent.Phase;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.IrhosAnimationComponent.Action;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.IrhosAnimationComponent.Direction;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.IrhosAnimationResources;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/** Real PNG decoding, native Box2D and production boss systems; no display required. */
public final class IrhosRegressionTest {
    private static int assertions;
    private static int textureCount;
    private static final Set<Integer> deletedTextures = new HashSet<>();
    private static final IrhosAnimationResources resources = new IrhosAnimationResources();

    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        initializeGraphics();
        verifyAssets();
        verifyMovementAndHurt();
        verifyTransitions();
        for (Direction direction : Direction.values()) {
            for (Phase phase : Phase.values()) verifyCombat(phase, direction);
            verifyDeath(direction);
        }
        require(textureCount == 36, "all bosses and retries share 36 textures");
        resources.dispose();
        resources.dispose();
        require(deletedTextures.size() == 36, "each texture disposed exactly once at screen cleanup");
        System.out.println("Irhos regressions passed: " + assertions + " assertions");
    }

    private static void verifyAssets() {
        resources.load();
        JsonValue manifest = new JsonReader().parse(Gdx.files.internal("characters/irhos/manifest.json"));
        String[] forms = {"IronFist", "BurningGauntlets", "DevilsCrown", "IrhosRevealed"};
        for (Phase phase : Phase.values()) {
            for (JsonValue sheet : manifest.get("combinedSheets").get(forms[phase.ordinal()])) {
                String name = sheet.getString("name");
                if (name.equals("DefeatedAlive")) continue;
                Action action = Action.valueOf(name);
                IrhosAnimationResources.Clip clip = resources.get(phase, action);
                int[] counts = sheet.get("rowCounts").asIntArray();
                require(clip.loop == (action == Action.Idle || action == Action.Walk), "loop policy");
                for (Direction direction : Direction.values()) {
                    TextureRegion[] row = clip.rows[direction.ordinal()];
                    require(row.length == counts[direction.ordinal()], "manifest valid row counts");
                    for (int i = 0; i < row.length; i++) {
                        TextureRegion frame = row[i];
                        require(frame.getRegionWidth() == 100 && frame.getRegionHeight() == 100
                            && frame.getRegionX() == i * 100 && frame.getRegionY() == direction.ordinal() * 100,
                            "100x100 slicing and Down/Left/Right/Up rows");
                        require(!frame.isFlipX() && !frame.isFlipY(), "original directional artwork");
                        require(frame.getTexture().getMinFilter() == Texture.TextureFilter.Nearest
                            && frame.getTexture().getMagFilter() == Texture.TextureFilter.Nearest, "nearest filtering");
                    }
                    require(clip.atProgress(direction, 2f, 0) == row[row.length - 1], "finite clips clamp");
                }
            }
        }
        for (Direction side : new Direction[] {Direction.LEFT, Direction.RIGHT}) {
            IrhosAnimationResources.Clip rush = resources.get(Phase.IRHOS_REVEALED, Action.RushWindup);
            IrhosAnimationResources.Clip volley = resources.get(Phase.IRHOS_REVEALED, Action.VolleyWindup);
            require(rush.rows[side.ordinal()].length == 1 && volley.rows[side.ordinal()].length == 2,
                "short revealed windup rows exclude padding");
            for (int i = 0; i < 100; i++) {
                require(rush.atProgress(side, i / 99f, 0).getRegionX() == 0, "side rush holds one pose");
                require(volley.atProgress(side, i / 99f, 0).getRegionX() <= 100, "side volley uses two poses");
            }
        }
    }

    private static void verifyMovementAndHurt() throws Exception {
        try (Encounter c = new Encounter()) {
            c.data().attackCycleReady = true;
            VelocityComponent velocity = c.boss.getComponent(VelocityComponent.class);
            for (Phase phase : Phase.values()) {
                c.data().phase = phase;
                c.data().attackState = BossComponent.AttackState.PURSUIT;
                c.data().revealedState = BossComponent.RevealedState.PURSUIT;
                for (Direction direction : Direction.values()) {
                    float[] vector = vector(direction);
                    velocity.vx = vector[0]; velocity.vy = vector[1];
                    c.animate(0.01f);
                    require(c.visual().direction == direction && c.visual().action == Action.Walk, "walk direction");
                    velocity.vx = 0f; velocity.vy = 0f;
                    c.animate(0.01f);
                    require(c.visual().direction == direction && c.visual().action == Action.Idle, "idle preserves facing");
                    c.boss.getComponent(HealthComponent.class).current -= 1f;
                    String before = gameplaySnapshot(c);
                    c.animate(0.01f);
                    require(c.visual().action == Action.Hurt && c.visual().form == phase, "hurt follows actual HP loss/form");
                    require(before.equals(gameplaySnapshot(c)), "animation does not write gameplay state");
                    c.boss.getComponent(HealthComponent.class).current -= 1f;
                    c.animate(0.12f);
                    require(c.visual().hurtTimeRemaining < IrhosAnimationSystem.HURT_DURATION - 0.1f,
                        "successive hits do not restart the hurt animation");
                    c.animate(IrhosAnimationSystem.HURT_DURATION);
                    require(c.visual().action == Action.Idle, "hurt finishes once");
                }
            }
            require(IrhosAnimationComponent.facing(1f, 2f, Direction.DOWN) == Direction.UP, "dominant vertical axis");
            require(IrhosAnimationComponent.facing(-2f, 1f, Direction.DOWN) == Direction.LEFT, "dominant horizontal axis");
            require(IrhosAnimationComponent.facing(1f, -1f, Direction.UP) == Direction.DOWN, "ties use vertical axis");
        }
    }

    private static void verifyTransitions() {
        try (Encounter c = new Encounter()) {
            c.tick(0f);
            for (int i = 0; i < 3; i++) {
                Phase outgoing = Phase.values()[i];
                c.boss.getComponent(HealthComponent.class).current = BossComponent.MAX_HEALTH
                    * (BossComponent.BURNING_GAUNTLETS_THRESHOLD - 0.25f * i);
                c.tick(0f);
                require(c.data().phase == Phase.values()[i + 1], "75/50/25 phase threshold");
                require(c.data().transitionTimeRemaining == BossPhaseSystem.TRANSITION_DURATION,
                    "extended transformation timer");
                require(c.visual().form == outgoing && c.visual().action == (i == 2 ? Action.Reveal : Action.Transition),
                    "outgoing form supplies transformation");
                c.tick(BossPhaseSystem.TRANSITION_DURATION - 0.2f);
                require(c.visual().frame.getRegionX() == 500, "sixth transition pose before finish");
                c.tick(0.21f);
                require(c.visual().form == Phase.values()[i + 1] && c.visual().action == Action.Idle,
                    "new form idle on transition expiry before combat reinitializes");
                c.tick(0.01f);
            }
        }
    }

    private static void verifyCombat(Phase phase, Direction direction) throws Exception {
        try (Encounter c = new Encounter()) {
            c.data().phase = phase;
            c.placePlayer(direction);
            c.tick(0f);
            require(c.boss.getComponent(HealthComponent.class).maximum == 2000f,
                "five-times total boss HP");
            require(c.boss.getComponent(PhysicsComponent.class).body.getFixtureList().first().getShape().getRadius() == 0.8f,
                "sprite size does not affect physics radius");
            int attacks = phase == Phase.IRHOS_REVEALED ? 3 : 1;
            for (int attack = 0; attack < attacks; attack++) {
                c.placePlayer(direction);
                for (int step = 0; step < 250 && !isWindup(c.visual().action); step++) c.tick(0.01f);
                Action windup = c.visual().action;
                require(isWindup(windup), "attack commits");
                require(c.visual().direction == direction, "attack locks correct direction");
                float duration = c.visual().stateDuration;
                float expected = phase == Phase.IRON_FIST ? 1.05f : phase == Phase.BURNING_GAUNTLETS ? 0.45f
                    : phase == Phase.DEVILS_CROWN ? 0.8f : attack == 0 ? 0.65f : attack == 1 ? 0.32f : 0.55f;
                require(Math.abs(duration - expected) < 0.0001f, "original windup duration");
                if (phase == Phase.IRHOS_REVEALED) require(windup == (attack == 0 ? Action.SlamWindup
                    : attack == 1 ? Action.RushWindup : Action.VolleyWindup), "Slam -> Punch -> Crown cycle");
                c.placePlayer(opposite(direction));
                c.boss.getComponent(HealthComponent.class).current -= 1f;
                String before = gameplaySnapshot(c);
                c.animate(0f);
                require(c.visual().action == windup && before.equals(gameplaySnapshot(c)), "hurt cannot interrupt committed attack");
                c.tick(duration * 0.51f);
                require(c.visual().direction == direction, "moving player behind boss does not flip windup");
                int count = resources.get(phase, windup).rows[direction.ordinal()].length;
                require(c.visual().frame.getRegionX() == (int) (count * 0.51f) * 100, "windup uses gameplay progress");
                c.tick(duration * 0.5f);
                Action impact = windup == Action.SlamWindup ? Action.Slam : windup == Action.RushWindup ? Action.Rush
                    : phase == Phase.IRHOS_REVEALED ? Action.VolleyRelease : Action.Volley;
                require(c.visual().action == impact, "impact/release visible on gameplay event update");
                require(c.visual().frame.getRegionX() == (impact == Action.Slam || impact == Action.Volley ? 200 : 0),
                    "impact/release skips anticipation poses");
                require(c.visual().direction == direction, "impact retains facing");
                if (windup == Action.VolleyWindup) {
                    require(c.projectiles.size == 10, "12 radial slots minus 2-slot safe gap");
                    float expectedDamage = phase == Phase.IRHOS_REVEALED ? 18f : 16f;
                    for (Entity projectile : c.projectiles) {
                        require(projectile.getComponent(ProjectileComponent.class).damage == expectedDamage, "unchanged crown damage");
                        require(projectile.getComponent(ProjectileComponent.class).irhosRevealedEffect,
                            "both crown phases use animated projectile sprites");
                    }
                    require(c.data().crownSafeGap == (phase == Phase.IRHOS_REVEALED ? 6 : 0), "safe gap sequence intact");
                }
                if (impact == Action.Rush) {
                    c.tick(0.01f);
                    VelocityComponent velocity = c.boss.getComponent(VelocityComponent.class);
                    require(velocity.vx == c.data().punchDirectionX * c.data().punchSpeed
                        && velocity.vy == c.data().punchDirectionY * c.data().punchSpeed, "combat owns rush velocity");
                    c.tick(c.data().punchDuration);
                }
                Action recovery = windup == Action.SlamWindup ? Action.SlamRecovery
                    : windup == Action.RushWindup ? Action.RushRecovery : Action.VolleyRecovery;
                float recoveryDuration = c.visual().stateDuration;
                float expectedRecovery = phase == Phase.IRHOS_REVEALED ? 0.48f
                    : phase == Phase.IRON_FIST ? 1.25f : phase == Phase.BURNING_GAUNTLETS ? 0.7f : 0.65f;
                require(Math.abs(recoveryDuration - expectedRecovery) < 0.0001f, "original recovery duration");
                c.tick(recoveryDuration * 0.8f);
                require(c.visual().action == recovery && c.visual().direction == direction, "recovery holds attack facing");
                c.tick(recoveryDuration * 0.21f);
                require(c.visual().action == Action.Idle || c.visual().action == Action.Walk, "returns to pursuit");
            }
        }
    }

    private static void verifyDeath(Direction direction) {
        try (Encounter c = new Encounter()) {
            c.data().phase = Phase.IRHOS_REVEALED;
            c.visual().direction = direction;
            c.boss.getComponent(HealthComponent.class).current = 0f;
            c.tick(0.01f);
            require(c.visual().action == Action.Death && c.visual().deathElapsed == 0f, "death begins once at frame zero");
            require(!c.boss.getComponent(PhysicsComponent.class).body.isActive() && c.defeats == 1, "existing death/victory callback");
            require(!IrhosAnimationSystem.isDefeatPresentationComplete(c.visual()), "no immediate victory overlay");
            // Same presentation-only update used while combat is frozen before the overlay.
            c.animate(IrhosAnimationSystem.DEATH_DURATION - 0.1f);
            require(c.visual().action == Action.Death && c.visual().frame.getRegionX() == 500, "sixth death frame");
            c.animate(0.11f);
            require(c.visual().action == Action.Corpse && c.visual().frame.getRegionX() == 0, "death then one-frame corpse");
            require(!IrhosAnimationSystem.isDefeatPresentationComplete(c.visual()), "hold defeated pose before victory");
            c.animate(IrhosAnimationSystem.DEFEAT_HOLD_DURATION - 0.02f);
            require(!IrhosAnimationSystem.isDefeatPresentationComplete(c.visual()), "full hold duration is required");
            c.animate(0.02f);
            require(IrhosAnimationSystem.isDefeatPresentationComplete(c.visual()), "victory allowed after defeat and hold");
            c.animate(5f);
            require(c.visual().action == Action.Corpse && c.visual().direction == direction && c.defeats == 1,
                "corpse holds, death and rewards never replay");
        }
    }

    private static String gameplaySnapshot(Encounter c) throws Exception {
        StringBuilder snapshot = new StringBuilder();
        for (Object component : new Object[] {c.data(), c.boss.getComponent(EnemyAIComponent.class),
            c.boss.getComponent(VelocityComponent.class), c.boss.getComponent(HealthComponent.class)}) {
            for (Field field : component.getClass().getFields()) snapshot.append(field.get(component)).append(';');
        }
        return snapshot.toString();
    }

    private static boolean isWindup(Action action) {
        return action == Action.SlamWindup || action == Action.RushWindup || action == Action.VolleyWindup;
    }

    private static float[] vector(Direction direction) {
        switch (direction) {
            case DOWN: return new float[] {0f, -2f};
            case LEFT: return new float[] {-2f, 0f};
            case RIGHT: return new float[] {2f, 0f};
            default: return new float[] {0f, 2f};
        }
    }

    private static Direction opposite(Direction direction) {
        return direction == Direction.DOWN ? Direction.UP : direction == Direction.UP ? Direction.DOWN
            : direction == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
    }

    private static void initializeGraphics() {
        Gdx.app = (Application) proxy(Application.class);
        Gdx.graphics = (Graphics) proxy(Graphics.class);
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(), new Class<?>[] {Files.class},
            (p, method, values) -> new FileHandle("assets/" + values[0]));
        Gdx.gl20 = (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(), new Class<?>[] {GL20.class},
            (p, method, values) -> {
                if (method.getName().equals("glGenTexture")) return ++textureCount;
                if (method.getName().equals("glDeleteTexture"))
                    require(deletedTextures.add((Integer) values[0]), "texture disposed once");
                return defaultValue(method.getReturnType());
            });
        Gdx.gl = Gdx.gl20;
    }

    private static Object proxy(Class<?> type) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (p, method, values) -> defaultValue(method.getReturnType()));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        return null;
    }

    private static void require(boolean value, String label) {
        assertions++;
        if (!value) throw new AssertionError(label);
    }

    private static final class Encounter implements AutoCloseable {
        final World world = new World(new Vector2(), true);
        final Engine engine = new Engine();
        final Entity boss = EnemyFactory.createBoss(world, new Vector2());
        final Entity player = new Entity();
        final Array<Entity> projectiles = new Array<>();
        final IrhosAnimationSystem animation = new IrhosAnimationSystem(resources);
        int defeats;

        Encounter() {
            player.add(new PositionComponent(0f, -2f)).add(new HealthComponent(1000f))
                .add(new InvulnerabilityComponent()).add(new PlayerComponent()).add(new DefenseComponent())
                .add(new StatusEffectComponent()).add(new RunInventoryComponent());
            engine.addEntity(player);
            engine.addEntity(boss);
            engine.addSystem(new BossCombatSystem(engine, player, projectiles));
            engine.addSystem(new BossPhaseSystem());
            engine.addSystem(new DeathSystem(player, ignored -> defeats++));
            engine.addSystem(animation);
        }
        void placePlayer(Direction direction) {
            float[] v = vector(direction);
            player.getComponent(PositionComponent.class).x = v[0];
            player.getComponent(PositionComponent.class).y = v[1];
        }
        void tick(float delta) { engine.update(delta); }
        void animate(float delta) { animation.update(delta); }
        BossComponent data() { return boss.getComponent(BossComponent.class); }
        IrhosAnimationComponent visual() { return boss.getComponent(IrhosAnimationComponent.class); }
        @Override public void close() { world.dispose(); }
    }
}
