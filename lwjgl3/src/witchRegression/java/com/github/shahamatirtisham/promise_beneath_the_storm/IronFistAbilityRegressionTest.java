package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/** Headless production-system checks for the added Iron Fist abilities. */
public final class IronFistAbilityRegressionTest {
    private static int assertions;
    private static int textures;
    private static final Set<Integer> deleted = new HashSet<>();

    public static void main(String[] args) {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        initializeGraphics();
        verifySheets();
        verifySummonAndBlocks();
        verifyFourTrackingStrikes();
        System.out.println("Iron Fist ability regressions passed: " + assertions + " assertions");
    }

    private static void verifySheets() {
        IronFistFxResources fx = new IronFistFxResources();
        fx.load();
        verify(fx.summonCircle, 7);
        verify(fx.wizardStrike, 10);
        verify(fx.skeletonIdle, 6);
        verify(fx.skeletonWalk, 8);
        verify(fx.skeletonAttack01, 6);
        verify(fx.skeletonAttack02, 7);
        verify(fx.skeletonBlock, 4);
        verify(fx.skeletonDeath, 4);
        verify(fx.skeletonHurt, 4);
        verify(fx.skeletonSummon, 5);
        require(textures == 10, "ten supplied runtime strips loaded once");
        fx.dispose();
        fx.dispose();
        require(deleted.size() == 10, "FX textures disposed exactly once");
    }

    private static void verify(Animation<TextureRegion> animation, int frames) {
        require(animation.getKeyFrames().length == frames, "actual frame count");
        for (int i = 0; i < frames; i++) {
            TextureRegion frame = animation.getKeyFrames()[i];
            require(frame.getRegionX() == i * 100 && frame.getRegionY() == 0
                && frame.getRegionWidth() == 100 && frame.getRegionHeight() == 100,
                "100x100 horizontal cells");
            require(frame.getTexture().getMinFilter() == Texture.TextureFilter.Nearest,
                "nearest filter");
        }
    }

    private static void verifySummonAndBlocks() {
        try (Encounter encounter = new Encounter()) {
            require(encounter.abilities.requestDebugSkeletonSummon(), "key-6 production summon accepted");
            require(!encounter.abilities.requestDebugSkeletonSummon(), "second group refused while pending");
            require(encounter.effects().size == 3, "three summon circles immediately");
            encounter.advance(0.35f);
            require(encounter.skeletons().size == 0, "skeletons hidden during initial circle frames");
            encounter.advance(0.02f);
            require(encounter.skeletons().size == 3, "exactly three skeletons appear");

            Entity[] spawned = new Entity[3];
            for (int i = 0; i < 3; i++) spawned[i] = encounter.skeletons().get(i);
            PositionComponent bossPosition = encounter.boss.getComponent(PositionComponent.class);
            for (Entity skeleton : spawned) {
                PositionComponent position = skeleton.getComponent(PositionComponent.class);
                float dx = position.x - bossPosition.x;
                float dy = position.y - bossPosition.y;
                require(Math.abs((float) Math.sqrt(dx * dx + dy * dy)
                    - SkeletonComponent.SUMMON_SPACING) < 0.001f, "triangle radius");
                require(!skeleton.getComponent(PhysicsComponent.class).body.isActive(),
                    "inactive while emerging");
            }
            float side = SkeletonComponent.SUMMON_SPACING * (float) Math.sqrt(3f);
            for (int i = 0; i < 3; i++) {
                PositionComponent a = spawned[i].getComponent(PositionComponent.class);
                PositionComponent b = spawned[(i + 1) % 3].getComponent(PositionComponent.class);
                float dx = a.x - b.x, dy = a.y - b.y;
                require(Math.abs((float) Math.sqrt(dx * dx + dy * dy) - side) < 0.001f,
                    "equilateral triangle");
            }
            encounter.advance(0.61f);
            require(encounter.effects().size == 0, "summon circles clean up");
            for (Entity skeleton : spawned) {
                require(!skeleton.getComponent(SkeletonComponent.class).summoning
                    && skeleton.getComponent(PhysicsComponent.class).body.isActive(),
                    "skeleton becomes active after emergence");
            }
            require(!encounter.abilities.requestDebugSkeletonSummon(),
                "living group prevents accumulation");

            Entity target = spawned[0];
            PositionComponent targetPosition = target.getComponent(PositionComponent.class);
            PositionComponent playerPosition = encounter.player.getComponent(PositionComponent.class);
            playerPosition.x = targetPosition.x - 0.7f;
            playerPosition.y = targetPosition.y;
            FacingComponent facing = encounter.player.getComponent(FacingComponent.class);
            facing.x = 1f; facing.y = 0f;
            AttackComponent attack = encounter.player.getComponent(AttackComponent.class);
            float hp = target.getComponent(HealthComponent.class).current;
            for (int hit = 0; hit < 3; hit++) {
                attack.attackId++;
                attack.activeTimeRemaining = 1f;
                target.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
                encounter.damage.update(0f);
                if (hit < 2) require(target.getComponent(HealthComponent.class).current == hp,
                    "first two player attacks blocked");
            }
            require(target.getComponent(SkeletonComponent.class).playerAttackBlocksRemaining == 0,
                "two-block allowance exhausted");
            require(target.getComponent(HealthComponent.class).current == hp - attack.damage,
                "third player attack damages skeleton");
        }
    }

    private static void verifyFourTrackingStrikes() {
        try (Encounter encounter = new Encounter()) {
            BossComponent boss = encounter.boss.getComponent(BossComponent.class);
            boss.summonCooldownRemaining = 100f;
            boss.wizardCooldownRemaining = 0f;
            encounter.advance(0.01f);
            require(boss.attackState == BossComponent.AttackState.IRON_WIZARD_CAST,
                "wizard cast locks boss action");
            require(encounter.boss.getComponent(VelocityComponent.class).vx == 0f,
                "boss stationary during cast");
            encounter.advance(encounter.fx.summonCircle.getAnimationDuration() + 0.02f);

            float health = encounter.player.getComponent(HealthComponent.class).current;
            for (int strike = 0; strike < 4; strike++) {
                Entity effect = encounter.wizardEffect();
                require(effect != null, "tracking strike exists " + strike);
                PositionComponent playerPosition = encounter.player.getComponent(PositionComponent.class);
                PositionComponent strikePosition = effect.getComponent(PositionComponent.class);
                require(strikePosition.x == playerPosition.x && strikePosition.y == playerPosition.y,
                    "strike samples latest player position");
                playerPosition.x += 2f;
                playerPosition.y += strike % 2 == 0 ? 1f : -1f;
                encounter.advance(IronFistAbilitySystem.WIZARD_HIT_TIME + 0.01f);
                require(encounter.player.getComponent(HealthComponent.class).current == health,
                    "moving out of telegraph evades hit");
                encounter.advance(encounter.fx.wizardStrike.getAnimationDuration()
                    - IronFistAbilitySystem.WIZARD_HIT_TIME);
            }
            require(encounter.wizardEffect() == null && boss.wizardStrikesGenerated == 4,
                "exactly four effects finish");
            require(boss.wizardCooldownRemaining <= IronFistAbilitySystem.WIZARD_SEQUENCE_COOLDOWN
                && boss.wizardCooldownRemaining > IronFistAbilitySystem.WIZARD_SEQUENCE_COOLDOWN - 0.05f,
                "five-second cooldown starts after fourth effect");
        }
    }

    private static void initializeGraphics() {
        Gdx.app = (Application) proxy(Application.class);
        Gdx.graphics = (Graphics) proxy(Graphics.class);
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
            new Class<?>[] {Files.class}, (p, method, values) -> new FileHandle("assets/" + values[0]));
        Gdx.gl20 = (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(),
            new Class<?>[] {GL20.class}, (p, method, values) -> {
                if (method.getName().equals("glGenTexture")) return ++textures;
                if (method.getName().equals("glDeleteTexture"))
                    require(deleted.add((Integer) values[0]), "texture disposed once");
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
        final Entity player = player();
        final Entity boss = EnemyFactory.createBoss(world, new Vector2(5f, 5f));
        final Array<Entity> enemies = new Array<>();
        final IronFistFxResources fx = new IronFistFxResources();
        final IronFistAbilitySystem abilities;
        final DamageSystem damage;

        Encounter() {
            fx.load();
            enemies.add(boss);
            engine.addEntity(player);
            engine.addEntity(boss);
            BossComponent data = boss.getComponent(BossComponent.class);
            data.attackCycleReady = true;
            data.attackState = BossComponent.AttackState.PURSUIT;
            abilities = new IronFistAbilitySystem(player, world, enemies, fx);
            damage = new DamageSystem(player);
            engine.addSystem(abilities);
            engine.addSystem(new SkeletonSystem(player, world, enemies));
            engine.addSystem(damage);
        }

        void advance(float seconds) {
            int steps = Math.max(1, (int) Math.ceil(seconds / 0.01f));
            float step = seconds / steps;
            for (int i = 0; i < steps; i++) engine.update(step);
        }

        Array<Entity> effects() {
            Array<Entity> result = new Array<>();
            for (Entity entity : engine.getEntitiesFor(Family.all(IronFistEffectComponent.class).get()))
                result.add(entity);
            return result;
        }

        Array<Entity> skeletons() {
            Array<Entity> result = new Array<>();
            for (Entity entity : engine.getEntitiesFor(Family.all(SkeletonComponent.class).get()))
                result.add(entity);
            return result;
        }

        Entity wizardEffect() {
            for (Entity entity : effects())
                if (entity.getComponent(IronFistEffectComponent.class).type
                    == IronFistEffectComponent.Type.WIZARD_STRIKE) return entity;
            return null;
        }

        @Override public void close() {
            abilities.clear();
            for (Entity enemy : enemies) {
                PhysicsComponent physics = enemy.getComponent(PhysicsComponent.class);
                if (physics != null && physics.body.getWorld() != null) world.destroyBody(physics.body);
            }
            fx.dispose();
            world.dispose();
        }
    }

    private static Entity player() {
        return new Entity().add(new PlayerComponent()).add(new PositionComponent(0f, 0f))
            .add(new VelocityComponent()).add(new HealthComponent(1000f))
            .add(new InvulnerabilityComponent()).add(new DefenseComponent())
            .add(new FacingComponent()).add(new AttackComponent()).add(new RunInventoryComponent())
            .add(new PlayerAnimationComponent());
    }
}
