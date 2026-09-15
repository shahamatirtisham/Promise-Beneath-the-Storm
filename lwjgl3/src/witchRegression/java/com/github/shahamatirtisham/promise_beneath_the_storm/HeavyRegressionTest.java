package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;

/** Exercises real Werebear assets, animation timelines, and production defensive impacts. */
public final class HeavyRegressionTest {
    private static int assertions;
    private static void require(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        java.lang.reflect.Method setup = ShieldGuardRegressionTest.class
            .getDeclaredMethod("initializeHeadlessGraphics");
        setup.setAccessible(true);
        setup.invoke(null);
        for (float delta : new float[] {0.01f, 0.2f, 1f}) {
            for (int variant = 0; variant < 3; variant++) {
                verifyAttack(variant, delta, false, false, false);
            }
        }
        verifyAttack(1, 0.01f, true, false, false);
        verifyAttack(1, 0.01f, false, true, false);
        verifyAttack(1, 0.01f, false, false, true);
        verifySequenceAndReactions();
        EnemyFactory.disposeWerebearAnimations();
        EnemyFactory.disposeWerebearAnimations();
        System.out.println("Heavy regressions passed: " + assertions + " assertions");
    }
    private static final class Combat implements AutoCloseable {
        final World world = new World(new Vector2(), true);
        final Engine engine = new Engine();
        final Entity player = new Entity();
        final Entity enemy = EnemyFactory.createHeavy(world, new Vector2());
        Combat() {
            player.add(new PositionComponent(1f, 0f));
            player.add(new PlayerComponent());
            player.add(new HealthComponent(1000f));
            player.add(new InvulnerabilityComponent());
            player.add(new DefenseComponent());
            FacingComponent facing = new FacingComponent();
            facing.x = -1f; facing.y = 0f;
            player.add(facing);
            ai().attackRange = 1.8f;
            ai().recoveryDuration = 1.1f;
            engine.addEntity(enemy);
            engine.addSystem(new HeavyEnemySystem(player));
            engine.addSystem(new EnemyAttackSystem(player, () -> 1));
            engine.addSystem(new EnemyAnimationSystem());
        }
        EnemyAIComponent ai() { return enemy.getComponent(EnemyAIComponent.class); }
        HeavyEnemyComponent heavy() { return enemy.getComponent(HeavyEnemyComponent.class); }
        AnimationComponent visual() { return enemy.getComponent(AnimationComponent.class); }
        float hp() { return player.getComponent(HealthComponent.class).current; }
        void step(float delta) {
            player.getComponent(InvulnerabilityComponent.class).timeRemaining = 0f;
            engine.update(delta);
        }
        public void close() { world.dispose(); }
    }
    private static void verifyAttack(int variant, float delta, boolean dodge, boolean block, boolean parry) {
        try (Combat c = new Combat()) {
            if (variant < 2) {
                c.heavy().attack03CooldownRemaining = 100f;
                c.heavy().nextMeleeVariant = HeavyEnemyComponent.Attack.values()[variant];
            }
            c.player.getComponent(DefenseComponent.class).blocking = block || parry;
            c.player.getComponent(DefenseComponent.class).parryTimeRemaining = parry ? 100f : 0f;
            c.step(delta); c.step(delta);
            require(c.heavy().currentAttack.ordinal() == variant, "selected requested variant");
            int hits = 0, iterations = 0;
            while (c.heavy().attackCommitted && iterations++ < 1000) {
                float before = c.hp();
                c.step(delta);
                if (before > c.hp()) {
                    hits++;
                    int frame = c.visual().attack.getKeyFrameIndex(c.visual().stateTime);
                    require(variant == 0 ? frame == 5 : variant == 1
                        ? (frame >= 3 && frame <= 4) || (frame >= 8 && frame <= 9) : frame == 4,
                        "damage occurs on exact visible source frame/window");
                    require(Math.abs(before - c.hp() - (block ? 6f : 15f)) < 0.001f,
                        "each impact uses standard damage/block reduction");
                    if (dodge) c.player.getComponent(PositionComponent.class).x = 5f;
                }
            }
            require(iterations < 1000, "attack completes even with parry stuns");
            require(hits == (parry ? 0 : dodge ? 1 : variant == 1 ? 2 : 1), "bounded once-only impacts and dodge");
            require(c.heavy().lastFrameShown, "final attack frame presented before recovery");
            require(c.ai().state == EnemyAIComponent.State.RECOVER, "full animation then recovery");
        }
    }
    private static void verifySequenceAndReactions() {
        try (Combat c = new Combat()) {
            Entity second = EnemyFactory.createHeavy(c.world, new Vector2(6f, 6f));
            require(c.visual().idle == second.getComponent(AnimationComponent.class).idle, "shared resources");
            int[] counts = {9, 13, 9};
            for (int i = 0; i < 3; i++) {
                require(c.visual().attackVariants[i].getKeyFrames().length == counts[i], "exact attack counts");
                require(c.visual().attackVariants[i].getFrameDuration() == 0.12f, "exact attack speed");
            }
            c.step(0.01f); c.step(0.01f);
            require(c.heavy().currentAttack == HeavyEnemyComponent.Attack.ATTACK03, "first attack special");
            require(c.heavy().attack03CooldownRemaining == 10f, "named ten second cooldown");
            float timeline = c.heavy().attackAnimationTime;
            c.enemy.getComponent(HealthComponent.class).current -= 1f;
            c.step(0.01f);
            require(c.visual().state == AnimationComponent.State.HURT && c.visual().stateTime == 0f,
                "actual damage starts hurt once at frame zero");
            timeline = c.heavy().attackAnimationTime;
            c.step(0.1f);
            require(c.heavy().attackAnimationTime == timeline, "hurt pauses committed timeline");
            require(c.heavy().attack03CooldownRemaining < 10f, "hurt does not reset cooldown");
            while (c.heavy().attackCommitted) c.step(0.01f);
            while (c.ai().state == EnemyAIComponent.State.RECOVER) c.step(0.01f);
            c.step(0.01f);
            require(c.heavy().currentAttack == HeavyEnemyComponent.Attack.ATTACK01, "first melee opportunity attack01");
            while (c.heavy().attackCommitted) c.step(0.01f);
            while (c.ai().state == EnemyAIComponent.State.RECOVER) c.step(0.01f);
            c.step(0.01f);
            require(c.heavy().currentAttack == HeavyEnemyComponent.Attack.ATTACK02, "second melee opportunity attack02");
            while (c.heavy().attackCommitted) c.step(0.01f);
            while (c.ai().state == EnemyAIComponent.State.RECOVER) c.step(0.01f);
            c.heavy().attack03CooldownRemaining = 0f;
            c.step(0.01f);
            require(c.heavy().currentAttack == HeavyEnemyComponent.Attack.ATTACK03, "ready special regains priority");
            boolean facing = c.visual().facingLeft;
            c.player.getComponent(PositionComponent.class).x = -1f;
            c.step(0.01f);
            require(c.visual().facingLeft == facing, "committed facing locked");
            c.enemy.getComponent(HealthComponent.class).current = 0f;
            c.step(0.01f);
            require(c.visual().stateTime == 0f && c.heavy().deathShadowTime == 0f, "death and shadow start at zero");
            c.step(1f); c.step(1f);
            require(c.visual().stateTime == 2f && c.heavy().deathShadowTime == 2f, "death timelines never restart");
            require(c.heavy().deathShadow.getKeyFrameIndex(c.heavy().deathShadowTime) == 3, "death shadow holds final frame");
        }
    }
}
