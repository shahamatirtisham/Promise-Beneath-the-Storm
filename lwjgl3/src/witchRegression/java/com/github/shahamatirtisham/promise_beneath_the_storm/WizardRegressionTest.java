package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;

/** Real sheet decoding and production spell/movement/defense lifecycles without a window. */
public final class WizardRegressionTest {
    private static int assertions;
    private static void require(boolean value, String label) {
        assertions++;
        if (!value) throw new AssertionError(label);
    }
    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Box2D.init();
        java.lang.reflect.Method setup = ShieldGuardRegressionTest.class.getDeclaredMethod("initializeHeadlessGraphics");
        setup.setAccessible(true); setup.invoke(null);
        sheetsAndSeparation();
        distanceOnlySpellSelection();
        for (float delta : new float[] {0.01f, 0.2f, 1f}) {
            cast(false, delta); cast(true, delta);
            crystal(false, delta); crystal(true, delta);
        }
        fireball(false, false, false);
        fireball(true, false, false);
        fireball(false, true, false);
        fireball(false, false, true);
        wallAndLifetime();
        limitedTrackingThenStraight();
        kitingAndReactions();
        WizardResources.dispose(); WizardResources.dispose();
        System.out.println("Wizard regressions passed: " + assertions + " assertions");
    }
    private static final class Combat implements AutoCloseable {
        final World world = new World(new Vector2(), true);
        final Engine engine = new Engine();
        final Entity player = new Entity();
        final Entity enemy = EnemyFactory.createRanged(world, new Vector2());
        final WizardSpellSystem spells = new WizardSpellSystem(player, world, () -> 1,
            () -> new Rectangle(-20f, -20f, 40f, 40f));
        Combat(boolean ai) {
            FacingComponent facing = new FacingComponent(); facing.x = -1f; facing.y = 0f;
            player.add(new PositionComponent(6f, 0f)).add(new HealthComponent(100f))
                .add(new PlayerComponent()).add(new InvulnerabilityComponent()).add(new StatusEffectComponent())
                .add(new DefenseComponent()).add(facing).add(new VelocityComponent());
            engine.addEntity(player); engine.addEntity(enemy);
            if (ai) {
                engine.addSystem(new RangedMovementSystem(player));
                engine.addSystem(new WizardSystem(player, spells));
                engine.addSystem(new RangedAttackSystem(engine, player, new com.badlogic.gdx.utils.Array<>()));
            }
            engine.addSystem(spells);
            engine.addSystem(new EnemyAnimationSystem());
        }
        WizardComponent wizard() { return enemy.getComponent(WizardComponent.class); }
        AnimationComponent visual() { return enemy.getComponent(AnimationComponent.class); }
        float hp() { return player.getComponent(HealthComponent.class).current; }
        Entity effect(Class<? extends Component> type) {
            return engine.getEntitiesFor(Family.all(type).get()).first();
        }
        int count(Class<? extends Component> type) { return engine.getEntitiesFor(Family.all(type).get()).size(); }
        void step(float delta) { engine.update(delta); }
        public void close() { spells.clear(); world.dispose(); }
    }
    private static void sheet(Animation<TextureRegion> a, int count, float duration, boolean loop) {
        require(a.getKeyFrames().length == count && a.getFrameDuration() == duration, "sheet count/speed");
        require(a.getPlayMode() == (loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL), "sheet play mode");
        for (TextureRegion f : a.getKeyFrames()) require(f.getRegionWidth() == 100 && f.getRegionHeight() == 100
            && f.getRegionY() == 0 && f.getTexture().getMinFilter() == Texture.TextureFilter.Nearest, "100px horizontal nearest cells");
    }
    private static void sheetsAndSeparation() {
        try (Combat c = new Combat(false)) {
            sheet(WizardResources.idle(), 6, 0.14f, true);
            sheet(WizardResources.walk(), 8, 0.11f, true);
            sheet(WizardResources.hurt(), 4, 0.1f, false);
            sheet(WizardResources.death(), 4, 0.14f, false);
            sheet(WizardResources.attack01(), 6, 0.12f, false);
            sheet(WizardResources.attack02(), 6, 0.12f, false);
            sheet(WizardResources.crystal(), 10, 0.12f, false);
            sheet(WizardResources.flight(), 4, 0.1f, true);
            sheet(WizardResources.impact(), 3, 0.1f, false);
            sheet(WizardResources.wallImpact(), 2, 0.1f, false);
            require(WizardResources.wallImpact().getKeyFrames()[0].getRegionX() == 500
                && WizardResources.wallImpact().getKeyFrames()[1].getRegionX() == 600,
                "wall dissipation uses final source frames6-7 only");
            for (int i = 0; i < 4; i++) require(WizardResources.flight().getKeyFrames()[i].getRegionX() == i * 100, "flight only source 1-4");
            for (int i = 0; i < 3; i++) require(WizardResources.impact().getKeyFrames()[i].getRegionX() == (i + 4) * 100, "impact only source 5-7");
            Entity second = EnemyFactory.createRanged(c.world, new Vector2(10f, 10f));
            require(second.getComponent(AnimationComponent.class).idle == c.visual().idle, "Wizard sheets shared");
            Entity necro = EnemyFactory.createNecromancer(c.world, new Vector2(12f, 12f));
            require(necro.getComponent(WizardComponent.class) == null && necro.getComponent(NecromancerComponent.class) != null,
                "Necromancer has no Wizard state");
            require(necro.getComponent(AnimationComponent.class).idle != c.visual().idle, "Necromancer keeps its own sprites");
            require(necro.getComponent(RangedEnemyComponent.class).attackCooldown == 2f, "Necromancer tuning unchanged");
            necro.getComponent(AnimationComponent.class).sourceTexture.dispose();
        }
    }
    private static void cast(boolean crystal, float delta) {
        try (Combat c = new Combat(true)) {
            c.player.getComponent(PositionComponent.class).x = crystal ? 4.5f : 7.5f;
            c.wizard().attack02CooldownRemaining = 0f;
            c.step(delta);
            require(c.wizard().state == (crystal ? WizardComponent.State.CAST_ATTACK01 : WizardComponent.State.CAST_ATTACK02), "distance selects correct spell");
            require(c.visual().stateTime == 0f, "cast starts at zero");
            c.player.getComponent(PositionComponent.class).x = -6f;
            int ticks = 0;
            Class<? extends Component> type = crystal ? WizardCrystalComponent.class : WizardFireballComponent.class;
            while (c.wizard().isCasting() && ticks++ < 1000) {
                boolean released = c.wizard().effectReleased;
                c.step(delta);
                require(!c.visual().facingLeft, "cast facing remains locked after target crosses behind");
                if (!released && c.wizard().effectReleased) {
                    require(c.visual().attack.getKeyFrameIndex(c.visual().stateTime) == 4, "effect release at cast source frame5");
                    require(c.count(type) == 1, "single effect release");
                    if (crystal) require(c.effect(type).getComponent(PositionComponent.class).x == 4.5f, "crystal uses initial locked target within cast range");
                }
            }
            require(ticks < 1000 && c.wizard().lastFrameShown, "full cast final frame then recovery");
            require(c.wizard().effectReleased, "cast released exactly once");
            require(c.engine.getEntitiesFor(Family.all(ProjectileComponent.class).get()).size() == 0, "no legacy shot alongside Wizard cast");
        }
    }
    private static void distanceOnlySpellSelection() {
        for (float distance : new float[] {2f, 3.2f, 4f, 5f, 5.1f, 8f, 8.1f}) {
            for (boolean crystalReady : new boolean[] {false, true}) {
                try (Combat c = new Combat(true)) {
                    c.player.getComponent(PositionComponent.class).x = distance;
                    c.wizard().attack01CooldownRemaining = crystalReady ? 0f : 100f;
                    c.wizard().attack02CooldownRemaining = 0f;
                    c.step(0.01f);
                    WizardComponent.State expected = distance < 3.2f || distance > 8f
                        ? WizardComponent.State.NONE : distance <= 5f
                            ? (crystalReady ? WizardComponent.State.CAST_ATTACK01 : WizardComponent.State.NONE)
                            : WizardComponent.State.CAST_ATTACK02;
                    require(c.wizard().state == expected,
                        "crystal exclusively close/moderate; fireball exclusively distant regardless of crystal cooldown");
                    if (distance >= 3.2f && distance <= 5f && !crystalReady)
                        require(c.enemy.getComponent(VelocityComponent.class).vx != 0f
                            || c.enemy.getComponent(VelocityComponent.class).vy != 0f,
                            "nearby cooling crystal preserves kiting instead of firing");
                }
            }
        }
    }

    private static void crystal(boolean dodge, float delta) {
        try (Combat c = new Combat(false)) {
            c.player.getComponent(PositionComponent.class).x = dodge ? 8f : 4f;
            c.spells.spawnCrystal(4f, 0f, 0f, 0f, 15f);
            Entity effect = c.effect(WizardCrystalComponent.class);
            WizardCrystalComponent spell = effect.getComponent(WizardCrystalComponent.class);
            int hits = 0, ticks = 0;
            while (c.count(WizardCrystalComponent.class) > 0 && ticks++ < 1000) {
                float before = c.hp(); c.step(delta);
                require(effect.getComponent(PositionComponent.class).x == 4f, "crystal never follows player");
                if (c.hp() < before) {
                    hits++;
                    require(WizardResources.crystal().getKeyFrameIndex(spell.stateTime) == 6, "crystal damage exclusively source frame7");
                }
                if (WizardResources.crystal().getKeyFrameIndex(spell.stateTime) < 6) require(c.hp() == 100f, "formation frames never damage");
            }
            require(ticks < 1000 && hits == (dodge ? 0 : 1), "single blast or dodge miss then complete cleanup");
            require(spell.lastFrameShown, "crystal completes final dissipation frame");
        }
    }
    private static void fireball(boolean block, boolean parry, boolean immune) {
        try (Combat c = new Combat(false)) {
            c.player.getComponent(PositionComponent.class).x = 2f;
            c.player.getComponent(DefenseComponent.class).blocking = block || parry;
            c.player.getComponent(DefenseComponent.class).parryTimeRemaining = parry ? 10f : 0f;
            c.player.getComponent(InvulnerabilityComponent.class).timeRemaining = immune ? 10f : 0f;
            c.spells.spawnFireball(0f, 0f, 1f, 0f, 15f);
            Entity effect = c.effect(WizardFireballComponent.class);
            WizardFireballComponent fireball = effect.getComponent(WizardFireballComponent.class);
            int ticks = 0;
            while (fireball.state == WizardFireballComponent.State.FLYING && ticks++ < 1000) c.step(0.01f);
            require(ticks < 1000 && fireball.stateTime == 0f, "contact immediately starts impact at source frame5");
            require(Math.abs(c.hp() - (parry || immune ? 100f : block ? 94f : 85f)) < 0.001f, "projectile defenses retained");
            require(c.player.getComponent(StatusEffectComponent.class).stunTime == (parry || immune ? 0f : 0.45f), "stun only on successful damage");
            float x = effect.getComponent(PositionComponent.class).x, hp = c.hp();
            while (c.count(WizardFireballComponent.class) > 0 && ticks++ < 1000) {
                c.step(0.01f);
                require(effect.getComponent(PositionComponent.class).x == x && c.hp() == hp, "impact is stationary and cannot damage again");
            }
            require(ticks < 1000 && fireball.lastFrameShown, "impact finishes all source frames before removal");
            c.engine.addSystem(new StatusEffectSystem()); c.step(0.5f);
            require(!c.player.getComponent(StatusEffectComponent.class).isStunned()
                && !c.player.getComponent(PlayerComponent.class).controlsLocked, "existing timer safely releases stun");
        }
    }
    private static void wallAndLifetime() {
        try (Combat c = new Combat(false)) {
            com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils.createStaticRectangle(c.world,
                new Rectangle(1f, -2f, 0.1f, 4f));
            c.player.getComponent(PositionComponent.class).x = 2f;
            c.spells.spawnFireball(0f, 0f, 1f, 0f, 15f);
            Entity effect = c.effect(WizardFireballComponent.class);
            c.step(0.01f); c.step(1f);
            require(effect.getComponent(WizardFireballComponent.class).state == WizardFireballComponent.State.IMPACT
                && c.hp() == 100f, "swept solid wall stops long-frame fireball without player damage");
            WizardFireballComponent wallSpell = effect.getComponent(WizardFireballComponent.class);
            require(wallSpell.wallImpact && wallSpell.stateTime == 0f, "wall starts its separate two-frame impact once");
            c.step(0.1f);
            require(c.count(WizardFireballComponent.class) == 1, "wall final frame shown before cleanup");
            c.step(0.11f);
            require(c.count(WizardFireballComponent.class) == 0 && wallSpell.lastFrameShown,
                "both final wall frames finish and disappear without replay");
            c.spells.clear();
            require(c.count(WizardFireballComponent.class) == 0, "room cleanup removes spells");
        }
        try (Combat c = new Combat(false)) {
            c.player.getComponent(PositionComponent.class).x = 15f;
            c.spells.spawnFireball(0f, 0f, -1f, 0f, 15f);
            Entity effect = c.effect(WizardFireballComponent.class);
            c.step(0.01f); c.step(0.01f);
            float angle = effect.getComponent(WizardFireballComponent.class).angleDegrees;
            require(Math.abs(angle - 180f) <= 2.401f, "homing rotation bounded by turn rate");
            effect.getComponent(WizardFireballComponent.class).lifetimeRemaining = 0.01f;
            c.step(0.02f);
            require(c.count(WizardFireballComponent.class) == 0, "flight lifetime safely expires");
        }
    }
    private static void limitedTrackingThenStraight() {
        try (Combat c = new Combat(false)) {
            PositionComponent target = c.player.getComponent(PositionComponent.class);
            target.x = 15f;
            c.spells.spawnFireball(0f, 0f, 1f, 0f, 15f);
            Entity effect = c.effect(WizardFireballComponent.class);
            WizardFireballComponent spell = effect.getComponent(WizardFireballComponent.class);
            c.step(0.01f); c.step(1.05f);
            require(!spell.trackingLost, "homing continues beyond one second");
            target.y = 8f;
            c.step(0.15f);
            require(!spell.trackingLost && spell.angleDegrees > 0f,
                "fireball follows moved player until 1.25 seconds");
            c.step(0.1f);
            require(spell.trackingLost && spell.trackingTime == 1.25f, "tracking ends at exactly 1.25 seconds");
            float heading = spell.angleDegrees;
            PositionComponent position = effect.getComponent(PositionComponent.class);
            float x = position.x, y = position.y;
            target.x = -15f; target.y = -15f;
            c.step(0.2f);
            require(spell.angleDegrees == heading, "after 1.25 seconds heading never turns or reacquires player");
            require(Math.abs(position.x - x - MathUtils.cosDeg(heading)) < 0.001f
                && Math.abs(position.y - y - MathUtils.sinDeg(heading)) < 0.001f,
                "post-homing flight continues straight at unchanged speed");
            require(c.hp() == 100f, "moving away can evade the committed straight shot");
        }
    }

    private static void kitingAndReactions() {
        try (Combat c = new Combat(true)) {
            c.wizard().attack01CooldownRemaining = c.wizard().attack02CooldownRemaining = 100f;
            c.player.getComponent(PositionComponent.class).x = 2f; c.step(0.01f);
            require(c.enemy.getComponent(VelocityComponent.class).vx < 0f && !c.wizard().isCasting(), "too-close Wizard retreats");
            require(c.visual().state == AnimationComponent.State.WALK, "retreat uses walk");
            c.player.getComponent(PositionComponent.class).x = 9f; c.step(0.01f);
            require(c.enemy.getComponent(VelocityComponent.class).vx > 0f, "too-far Wizard approaches");
            c.player.getComponent(PositionComponent.class).x = 4f; c.step(0.01f);
            require(c.enemy.getComponent(VelocityComponent.class).vy > 0f, "preferred range strafes");
            c.step(1.3f);
            require(c.enemy.getComponent(VelocityComponent.class).vy < 0f, "strafe direction still repositions");
            c.wizard().attack01CooldownRemaining = 0f; c.step(0.01f);
            c.enemy.getComponent(HealthComponent.class).current -= 1f; c.step(0.01f);
            require(c.visual().state == AnimationComponent.State.HURT && c.visual().stateTime == 0f, "actual health loss starts one-shot hurt");
            float clock = c.wizard().castAnimationTime, cooldown = c.wizard().attack01CooldownRemaining;
            c.step(0.1f);
            require(c.wizard().castAnimationTime == clock && c.wizard().attack01CooldownRemaining < cooldown, "hurt pauses cast and preserves ticking cooldown");
            int ticks = 0;
            while (c.wizard().isCasting() && ticks++ < 1000) c.step(0.01f);
            require(ticks < 1000 && c.wizard().lastFrameShown, "cast resumes and finishes after hurt");
            c.enemy.getComponent(HealthComponent.class).current = 0f; c.step(0.01f);
            require(c.visual().state == AnimationComponent.State.DEAD && c.visual().stateTime == 0f, "death starts once at zero");
            c.step(1f); c.step(1f);
            require(c.visual().stateTime == 2f && c.visual().death.getKeyFrameIndex(2f) == 3, "death holds final frame without replay");
        }
    }
}
