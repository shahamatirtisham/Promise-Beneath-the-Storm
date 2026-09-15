package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
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
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/** Real asset decoding and production combat systems; GL calls are recorded without a window. */
public final class ShieldGuardRegressionTest {
    private static int assertions;
    private static int textureCount;
    private static final Set<Integer> deletedTextures = new HashSet<>();

    public static void main(String[] args) {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        initializeHeadlessGraphics();
        verifySheetsAndSharing();
        verifyMelee(1f, 0f, false, false);
        verifyMelee(1f, 0f, true, false);
        verifyMelee(-1f, 0f, false, false);
        verifyMelee(0f, 1f, false, false);
        verifyMelee(1f, 0f, false, true);
        verifyKnife(0.5f, 0f, false);
        verifyKnife(-0.5f, 0f, false);
        verifyKnife(0f, 0.5f, false);
        verifyKnife(0.5f, 0f, true);
        verifyParryAndFacing();
        verifySynchronizedAttackTiming(0.01f);
        verifySynchronizedAttackTiming(0.2f);
        verifySynchronizedAttackTiming(1f);
        verifyAttackReactionPause();
        verifyUnawareFacing();
        verifyAnimationCycleAndPriority();
        EnemyFactory.disposeShieldGuardAnimations();
        require(deletedTextures.size() == 8, "eight unique source textures disposed");
        EnemyFactory.disposeShieldGuardAnimations();
        require(deletedTextures.size() == 8, "repeat cleanup does not dispose twice");
        System.out.println("Shield Guard regressions passed: " + assertions + " assertions");
    }

    private static void verifySheetsAndSharing() {
        try (Combat combat = new Combat()) {
            Entity second = EnemyFactory.createShieldGuard(combat.world, new Vector2(5f, 5f));
            AnimationComponent a = combat.visual();
            AnimationComponent b = second.getComponent(AnimationComponent.class);
            require(a.idle == b.idle && a.attackVariants[2] == b.attackVariants[2], "animations shared");
            require(textureCount == 8, "only eight textures loaded across guards");
            verifySheet(a.idle, 6, 0.18f, true);
            verifySheet(a.walk, 8, 0.16f, true);
            verifySheet(a.block, 4, 0.12f, false);
            verifySheet(a.hurt, 5, 0.12f, false);
            verifySheet(a.death, 4, 0.18f, false);
            for (int i = 0; i < 3; i++) verifySheet(a.attackVariants[i], 7 + i, 0.14f, false);
            require(a.renderWidth == 5.2f && a.renderHeight == 5.2f && a.renderYOffset == 1.9f,
                "guard has its own Orc-sized render settings");
            require(!a.sourceFacesLeft, "native facing right");
        }
    }

    private static void verifySheet(Animation<TextureRegion> animation, int count,
        float duration, boolean loop) {
        require(animation.getKeyFrames().length == count, "correct frame count");
        require(animation.getFrameDuration() == duration, "correct frame duration");
        require(animation.getPlayMode() == (loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL),
            "correct playback mode");
        for (int i = 0; i < count; i++) {
            TextureRegion frame = animation.getKeyFrames()[i];
            require(frame.getRegionX() == 100 * i && frame.getRegionY() == 0
                && frame.getRegionWidth() == 100 && frame.getRegionHeight() == 100,
                "horizontal 100x100 slicing");
            require(!frame.isFlipX() && frame.getTexture().getMinFilter() == Texture.TextureFilter.Nearest,
                "nearest filtering and unflipped shared region");
        }
    }

    private static void verifyMelee(float x, float y, boolean finisher, boolean broken) {
        try (Combat combat = new Combat()) {
            combat.placePlayer(x, y);
            FacingComponent facing = combat.player.getComponent(FacingComponent.class);
            facing.x = -x;
            facing.y = -y;
            AttackComponent attack = combat.player.getComponent(AttackComponent.class);
            attack.activeTimeRemaining = 1f;
            attack.attackId = 1;
            attack.knockbackStrength = finisher ? 3f : 0f;
            combat.shield().guardBrokenTimeRemaining = broken ? 1f : 0f;
            combat.engine.addSystem(new DamageSystem(combat.player));
            combat.engine.addSystem(new EnemyAnimationSystem());
            combat.engine.update(0.01f);
            boolean protects = x > 0f && !broken;
            require(combat.hp() == (protects ? 50f : 30f), "melee frontal/side/back/broken damage");
            require(combat.visual().state == (protects && !finisher ? AnimationComponent.State.BLOCK
                : AnimationComponent.State.HURT), "block versus actual hit/break visual");
            require(combat.visual().stateTime == 0f, "reaction starts at frame zero");
            float hp = combat.hp();
            combat.guard.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
            combat.engine.update(0.01f);
            require(combat.hp() == hp, "same swing cannot damage twice");
            if (finisher && protects) {
                require(combat.shield().guardBrokenTimeRemaining == combat.shield().comboBreakDuration,
                    "finisher retains existing break duration");
                require(combat.ai().state == EnemyAIComponent.State.STUNNED,
                    "finisher retains existing stun/knockback");
                attack.attackId++;
                attack.knockbackStrength = 0f;
                combat.engine.update(0.01f);
                require(combat.hp() == 30f, "next hit after zero-HP break deals full damage");
            }
        }
    }

    private static void verifyKnife(float x, float y, boolean broken) {
        try (Combat combat = new Combat()) {
            combat.shield().guardBrokenTimeRemaining = broken ? 1f : 0f;
            Entity knife = new Entity();
            knife.add(new PositionComponent(x, y)).add(new VelocityComponent())
                .add(new ProjectileComponent(12f, 1f, 0.1f))
                .add(new TeamComponent(TeamComponent.Team.PLAYER));
            Array<Entity> projectiles = new Array<>();
            projectiles.add(knife);
            Array<Entity> enemies = new Array<>();
            enemies.add(combat.guard);
            combat.engine.addEntity(knife);
            combat.engine.addSystem(new ProjectileSystem(combat.engine, combat.player,
                projectiles, enemies, () -> 1, null));
            combat.engine.addSystem(new EnemyAnimationSystem());
            combat.engine.update(0f);
            boolean protects = x > 0f && !broken;
            require(combat.hp() == (protects ? 50f : 38f), "knife frontal/side/back/broken damage");
            require(combat.visual().state == (protects ? AnimationComponent.State.BLOCK
                : AnimationComponent.State.HURT), "knife block/hit distinction");
            require(projectiles.size == 0, "blocked and damaging knives consumed normally");
        }
    }

    private static void verifyParryAndFacing() {
        try (Combat combat = new Combat()) {
            combat.placePlayer(1f, 0f);
            combat.engine.addSystem(new ShieldGuardSystem(combat.player));
            combat.engine.addSystem(new EnemyAttackSystem(combat.player, () -> 1));
            combat.engine.addSystem(new EnemyAnimationSystem());
            combat.ai().state = EnemyAIComponent.State.CHASE;
            combat.engine.update(0.01f);
            require(combat.shield().facingX == 1f && !combat.visual().facingLeft, "mobile facing agrees");
            combat.ai().state = EnemyAIComponent.State.ATTACK;
            combat.shield().beginAttack(combat.visual());
            combat.placePlayer(-1f, 0f);
            combat.engine.update(0.01f);
            require(combat.shield().facingX == 1f && !combat.visual().facingLeft, "committed facing locked");
            combat.ai().attackPending = true;
            combat.player.getComponent(FacingComponent.class).x = 1f;
            DefenseComponent defense = combat.player.getComponent(DefenseComponent.class);
            defense.blocking = true;
            defense.parryTimeRemaining = 0.1f;
            combat.engine.update(0.01f);
            require(combat.ai().state == EnemyAIComponent.State.STUNNED
                && combat.ai().stateTimeRemaining == 1f, "perfect parry retains stun");
            require(combat.shield().guardBrokenTimeRemaining == combat.shield().parryBreakDuration,
                "perfect parry retains break duration");
            require(combat.visual().state == AnimationComponent.State.HURT, "parry plays hit, not block");
            combat.ai().state = EnemyAIComponent.State.CHASE;
            combat.engine.update(4f);
            require(!combat.shield().isGuardBroken(), "shield automatically returns after timer");
            combat.shield().requestBlockVisual();
            combat.engine.update(0.01f);
            require(combat.visual().state == AnimationComponent.State.BLOCK, "blocking visual returns");
        }
    }

    private static void verifyAnimationCycleAndPriority() {
        try (Combat combat = new Combat()) {
            combat.engine.addSystem(new EnemyAnimationSystem());
            for (int i = 0; i < 4; i++) {
                combat.ai().state = EnemyAIComponent.State.ATTACK;
                combat.shield().beginAttack(combat.visual());
                combat.engine.update(0.01f);
                require(combat.visual().attack == combat.visual().attackVariants[i % 3], "01/02/03/01 cycle");
                require(combat.visual().stateTime == 0f, "new attack resets to frame zero");
                combat.engine.update(0.1f);
                require(combat.visual().attack == combat.visual().attackVariants[i % 3], "variant stays fixed");
                combat.ai().state = EnemyAIComponent.State.RECOVER;
                combat.engine.update(0.01f);
                require(combat.visual().state == AnimationComponent.State.IDLE, "recovery loops idle");
            }
            combat.shield().requestBlockVisual();
            combat.engine.update(0.01f);
            require(combat.visual().state == AnimationComponent.State.BLOCK, "local block event");
            combat.engine.update(0.1f);
            combat.shield().requestBlockVisual();
            combat.engine.update(0.01f);
            require(combat.visual().stateTime == 0f, "successive block resets frame zero");
            combat.engine.update(combat.visual().block.getAnimationDuration() + 0.1f);
            combat.engine.update(0.01f);
            require(combat.visual().state == AnimationComponent.State.IDLE, "block returns to current AI");
            combat.shield().requestBlockVisual();
            combat.engine.update(0.01f);
            combat.guard.getComponent(HealthComponent.class).current -= 1f;
            combat.engine.update(0.01f);
            require(combat.visual().state == AnimationComponent.State.HURT, "damaging hit overrides block");
            combat.guard.getComponent(HealthComponent.class).current = 0f;
            combat.engine.addSystem(new DeathSystem(combat.player, enemy -> {}));
            combat.engine.update(0.01f);
            require(combat.visual().state == AnimationComponent.State.DEAD && combat.visual().stateTime == 0f,
                "death overrides reactions and begins at zero");
            boolean[] frames = new boolean[4];
            for (int i = 0; i < 80; i++) {
                combat.engine.update(0.01f);
                frames[combat.visual().death.getKeyFrameIndex(combat.visual().stateTime)] = true;
            }
            for (boolean frame : frames) require(frame, "death shows all four frames");
            require(combat.visual().death.getKeyFrameIndex(combat.visual().stateTime) == 3,
                "death plays once and holds final frame");
            require(deletedTextures.isEmpty(), "death does not dispose shared resources");
        }
    }

    private static void verifySynchronizedAttackTiming(float delta) {
        try (Combat combat = new Combat()) {
            combat.engine.addSystem(new EnemyAISystem(combat.player));
            combat.engine.addSystem(new ShieldGuardSystem(combat.player));
            combat.engine.addSystem(new EnemyAttackSystem(combat.player, () -> 1));
            combat.engine.addSystem(new EnemyAnimationSystem());
            for (int attack = 0; attack < 4; attack++) {
                combat.player.getComponent(HealthComponent.class).current = 100f;
                combat.ai().state = EnemyAIComponent.State.CHASE;
                combat.engine.update(delta);
                Animation<TextureRegion> selected = combat.visual().attack;
                float duration = selected.getAnimationDuration();
                require(selected == combat.visual().attackVariants[attack % 3], "AI selects 01/02/03/01 once");
                require(combat.visual().stateTime == 0f, "AI commitment starts on frame zero");
                require(Math.abs(duration - (7 + attack % 3) * 0.14f) < 0.00001f,
                    "attack duration comes from selected animation");
                boolean[] shown = new boolean[selected.getKeyFrames().length];
                shown[0] = true;
                int hits = 0;
                int ticks = 0;
                while (combat.ai().state == EnemyAIComponent.State.ATTACK) {
                    require(ticks++ < 2000, "attack completes");
                    combat.player.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
                    float before = combat.player.getComponent(HealthComponent.class).current;
                    float previousTime = combat.shield().attackAnimationTime;
                    combat.engine.update(delta);
                    require(combat.visual().attack == selected, "variant stays selected for full attack");
                    require(combat.shield().attackAnimationTime >= previousTime, "attack clock never resets per update");
                    if (combat.player.getComponent(HealthComponent.class).current < before) {
                        hits++;
                        require(combat.shield().attackAnimationTime >= duration * 0.70f,
                            "attack damage occurs only at or after seventy percent");
                        require(combat.ai().state == EnemyAIComponent.State.ATTACK,
                            "attack continues after damage");
                    }
                    if (combat.shield().attackAnimationTime < duration * 0.70f) {
                        require(combat.player.getComponent(HealthComponent.class).current == 100f,
                            "heavy attack never damages early");
                    }
                    if (combat.visual().state == AnimationComponent.State.ATTACK) {
                        shown[selected.getKeyFrameIndex(combat.visual().stateTime)] = true;
                    }
                }
                for (boolean frame : shown) require(frame, "every attack frame is displayed");
                require(hits == 1, "attackPending queues one hit despite expiring invulnerability");
                require(selected.isAnimationFinished(combat.shield().attackAnimationTime),
                    "recovery only after selected animation finishes");
                require(combat.ai().state == EnemyAIComponent.State.RECOVER
                    && combat.visual().state == AnimationComponent.State.IDLE
                    && combat.ai().stateTimeRemaining == combat.ai().recoveryDuration,
                    "recovery duration preserved and idle selected");
            }
        }
    }

    private static void verifyUnawareFacing() {
        try (Combat combat = new Combat()) {
            combat.engine.addSystem(new EnemyAISystem(combat.player));
            combat.engine.addSystem(new ShieldGuardSystem(combat.player));
            combat.engine.addSystem(new EnemyAnimationSystem());
            combat.placePlayer(-10f, 0f);
            combat.engine.update(0.01f);
            require(combat.ai().state == EnemyAIComponent.State.IDLE
                && combat.shield().facingX == 1f && !combat.visual().facingLeft,
                "unaware guard does not face distant player");
            combat.placePlayer(10f, 0f);
            combat.engine.update(0.01f);
            require(combat.shield().facingX == 1f, "unaware facing stays stable on either side");
            combat.placePlayer(-2f, 0f);
            combat.engine.update(0.01f);
            require(combat.ai().state == EnemyAIComponent.State.CHASE
                && combat.shield().facingX == -1f && combat.visual().facingLeft,
                "detection enables facing updates");
        }
    }

    private static void verifyAttackReactionPause() {
        try (Combat combat = new Combat()) {
            combat.engine.addSystem(new EnemyAISystem(combat.player));
            combat.engine.addSystem(new ShieldGuardSystem(combat.player));
            combat.engine.addSystem(new EnemyAttackSystem(combat.player, () -> 1));
            combat.engine.addSystem(new EnemyAnimationSystem());
            combat.ai().state = EnemyAIComponent.State.CHASE;
            combat.engine.update(0.01f);
            combat.engine.update(0.2f);
            float clock = combat.shield().attackAnimationTime;
            combat.shield().requestBlockVisual();
            combat.engine.update(0.01f);
            for (int i = 0; i < 4; i++) {
                combat.engine.update(0.1f);
                require(combat.shield().attackAnimationTime == clock
                    && combat.ai().state == EnemyAIComponent.State.ATTACK,
                    "block reaction cannot consume attack frames");
            }
            combat.guard.getComponent(HealthComponent.class).current -= 1f;
            combat.engine.update(0.01f);
            for (int i = 0; i < 4; i++) {
                combat.engine.update(0.1f);
                require(combat.shield().attackAnimationTime == clock,
                    "non-stunning hurt reaction pauses attack clock");
            }
            int ticks = 0;
            while (combat.ai().state == EnemyAIComponent.State.ATTACK) {
                require(ticks++ < 2000, "attack resumes after reactions");
                combat.engine.update(0.01f);
            }
            require(combat.visual().attack.isAnimationFinished(combat.shield().attackAnimationTime)
                && combat.shield().attackLastFrameShown, "reaction-interrupted attack fully finishes");
        }
    }

    private static void initializeHeadlessGraphics() {
        Gdx.app = (Application) proxy(Application.class);
        Gdx.graphics = (Graphics) proxy(Graphics.class);
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(), new Class<?>[] {Files.class},
            (p, method, values) -> new FileHandle("assets/" + values[0]));
        Gdx.gl20 = (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(), new Class<?>[] {GL20.class},
            (p, method, values) -> {
                if (method.getName().equals("glGenTexture")) return ++textureCount;
                if (method.getName().equals("glDeleteTexture")) {
                    require(deletedTextures.add((Integer) values[0]), "texture disposed exactly once");
                }
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

    private static final class Combat implements AutoCloseable {
        final World world = new World(new Vector2(), true);
        final Engine engine = new Engine();
        final Entity guard = EnemyFactory.createShieldGuard(world, new Vector2());
        final Entity player = new Entity();

        Combat() {
            shield().facingX = 1f;
            player.add(new PositionComponent(1f, 0f)).add(new FacingComponent())
                .add(new HealthComponent(100f)).add(new InvulnerabilityComponent())
                .add(new PlayerComponent()).add(new AttackComponent()).add(new DefenseComponent())
                .add(new RunInventoryComponent());
            engine.addEntity(guard);
            engine.addEntity(player);
        }
        void placePlayer(float x, float y) {
            PositionComponent position = player.getComponent(PositionComponent.class);
            position.x = x;
            position.y = y;
        }
        float hp() { return guard.getComponent(HealthComponent.class).current; }
        ShieldGuardComponent shield() { return guard.getComponent(ShieldGuardComponent.class); }
        EnemyAIComponent ai() { return guard.getComponent(EnemyAIComponent.class); }
        AnimationComponent visual() { return guard.getComponent(AnimationComponent.class); }
        public void close() { world.dispose(); }
    }
}
