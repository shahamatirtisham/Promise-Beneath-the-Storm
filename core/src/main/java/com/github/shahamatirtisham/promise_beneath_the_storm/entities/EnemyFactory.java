package com.github.shahamatirtisham.promise_beneath_the_storm.entities;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AnimationComponent;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.KnockbackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RangedEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HeavyEnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChargerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.NecromancerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ShieldGuardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.WitchComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.WizardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

/** Creates the current placeholder melee-enemy archetype. */
public final class EnemyFactory {
    private static final float ENEMY_RADIUS = 0.45f;
    private static final java.util.Map<String, Animation<TextureRegion>> witchAnimations =
        new java.util.HashMap<>();
    private static final java.util.Map<String, Animation<TextureRegion>> armoredOrcAnimations =
        new java.util.HashMap<>();
    private static final float SHIELD_GUARD_RENDER_SIZE = 5.2f;
    private static final float SHIELD_GUARD_RENDER_Y_OFFSET = 2.9f;
    private static final float SHIELD_GUARD_IDLE_FRAME_DURATION = 0.18f;
    private static final float SHIELD_GUARD_WALK_FRAME_DURATION = 0.16f;
    private static final float SHIELD_GUARD_REACTION_FRAME_DURATION = 0.12f;
    private static final float SHIELD_GUARD_ATTACK_FRAME_DURATION = 0.14f;
    private static final float SHIELD_GUARD_DEATH_FRAME_DURATION = 0.18f;

    public static final float WEREBEAR_RENDER_SIZE = 5.8f;
    public static final float WEREBEAR_RENDER_Y_OFFSET = 3f;
    public static final float WEREBEAR_SHADOW_RENDER_WIDTH = 5.8f;
    public static final float WEREBEAR_SHADOW_RENDER_HEIGHT = 5.8f;
    public static final float WEREBEAR_SHADOW_Y_OFFSET = 2.9f;
    public static final float IDLE_FRAME_DURATION = 0.14f;
    public static final float WALK_FRAME_DURATION = 0.12f;
    public static final float HURT_FRAME_DURATION = 0.10f;
    public static final float DEATH_FRAME_DURATION = 0.14f;
    public static final float ATTACK01_FRAME_DURATION = 0.12f;
    public static final float ATTACK02_FRAME_DURATION = 0.12f;
    public static final float ATTACK03_FRAME_DURATION = 0.12f;
    private static final java.util.Map<String, Animation<TextureRegion>> werebearAnimations =
        new java.util.HashMap<>();

    private EnemyFactory() {
    }

    public static Entity createMelee(World world, Vector2 spawn) {

        Entity enemy = createBase(world, spawn, ENEMY_RADIUS);

        addOrcAnimations(enemy);

        return enemy;
    }

    private static void addOrcAnimations(Entity enemy) {

        AnimationComponent animation = new AnimationComponent();

        animation.idle =
            AnimationFactory.createAnimation(
                "characters/orc/Orc_Idle.png",
                0.12f
            );

        animation.walk =
            AnimationFactory.createAnimation(
                "characters/orc/Orc_Walk.png",
                0.10f
            );

        animation.attack =
            AnimationFactory.createAnimation(
                "characters/orc/Orc_Attack01.png",
                0.08f
            );

        animation.hurt =
            AnimationFactory.createAnimation(
                "characters/orc/Orc_Hurt.png",
                0.10f
            );

        animation.death =
            AnimationFactory.createAnimation(
                "characters/orc/Orc_Death.png",
                0.12f
            );

        animation.idle.setPlayMode(Animation.PlayMode.LOOP);
        animation.walk.setPlayMode(Animation.PlayMode.LOOP);

        animation.attack.setPlayMode(Animation.PlayMode.NORMAL);
        animation.hurt.setPlayMode(Animation.PlayMode.NORMAL);
        animation.death.setPlayMode(Animation.PlayMode.NORMAL);

        enemy.add(animation);
    }

    private static Entity createBase(World world, Vector2 spawn, float radius) {
        Body body = WorldUtils.createDynamicCircle(
            world,
            spawn.x,
            spawn.y,
            radius
        );

        Entity enemy = new Entity();
        enemy.add(new EnemyComponent());
        enemy.add(new EnemyAIComponent());
        enemy.add(new PositionComponent(spawn.x, spawn.y));
        enemy.add(new VelocityComponent());
        enemy.add(new PhysicsComponent(body));
        enemy.add(new HealthComponent(50f));
        enemy.add(new InvulnerabilityComponent());
        enemy.add(new TeamComponent(TeamComponent.Team.ENEMY));
        enemy.add(new KnockbackComponent());
        return enemy;
    }

    private static Entity createRangedBase(World world, Vector2 spawn) {
        Entity enemy = createBase(world, spawn, ENEMY_RADIUS);
        enemy.add(new RangedEnemyComponent());
        return enemy;
    }

    /** Normal RANGED spawns use Wizards; specialized ranged enemies use the base. */
    @SuppressWarnings("unchecked")
    public static Entity createRanged(World world, Vector2 spawn) {
        Entity enemy = createRangedBase(world, spawn);
        enemy.add(new WizardComponent());
        AnimationComponent animation = new AnimationComponent();
        animation.idle = WizardResources.idle();
        animation.walk = WizardResources.walk();
        animation.hurt = WizardResources.hurt();
        animation.death = WizardResources.death();
        animation.attackVariants = (Animation<TextureRegion>[]) new Animation<?>[] {
            WizardResources.attack01(), WizardResources.attack02()
        };
        animation.attack = animation.attackVariants[1];
        animation.renderWidth = WizardComponent.WIZARD_RENDER_SIZE;
        animation.renderHeight = animation.renderWidth;
        animation.renderYOffset = WizardComponent.WIZARD_RENDER_Y_OFFSET;
        animation.sourceFacesLeft = false;
        animation.previousHealth = enemy.getComponent(HealthComponent.class).current;
        enemy.add(animation);
        return enemy;
    }

    public static Entity createHeavy(World world, Vector2 spawn) {
        Entity enemy = createBase(world, spawn, 0.65f);
        enemy.add(new HeavyEnemyComponent());
        addWerebearAnimations(enemy);
        return enemy;
    }

    @SuppressWarnings("unchecked")
    private static void addWerebearAnimations(Entity enemy) {
        AnimationComponent animation = new AnimationComponent();
        animation.idle = werebearAnimation("Idle", 6, IDLE_FRAME_DURATION, true);
        animation.walk = werebearAnimation("Walk", 8, WALK_FRAME_DURATION, true);
        animation.hurt = werebearAnimation("Hurt", 4, HURT_FRAME_DURATION, false);
        animation.death = werebearAnimation("Death", 4, DEATH_FRAME_DURATION, false);
        animation.attackVariants = (Animation<TextureRegion>[]) new Animation<?>[] {
            werebearAnimation("Attack01", 9, ATTACK01_FRAME_DURATION, false),
            werebearAnimation("Attack02", 13, ATTACK02_FRAME_DURATION, false),
            werebearAnimation("Attack03", 9, ATTACK03_FRAME_DURATION, false)
        };
        animation.attack = animation.attackVariants[0];
        animation.renderWidth = WEREBEAR_RENDER_SIZE;
        animation.renderHeight = WEREBEAR_RENDER_SIZE;
        animation.renderYOffset = WEREBEAR_RENDER_Y_OFFSET;
        animation.sourceFacesLeft = false;
        animation.previousHealth = enemy.getComponent(HealthComponent.class).current;
        HeavyEnemyComponent heavy = enemy.getComponent(HeavyEnemyComponent.class);
        heavy.shadow = werebearAnimation("shadow", 1, 1f, false).getKeyFrames()[0];
        heavy.deathShadow = werebearAnimation("shadow_death", 4, DEATH_FRAME_DURATION, false);
        enemy.add(animation);
    }

    private static Animation<TextureRegion> werebearAnimation(String name, int count,
        float duration, boolean loop) {
        Animation<TextureRegion> cached = werebearAnimations.get(name);
        if (cached != null) return cached;
        String path = "characters/were_bear/Werebear-" + name + ".png";
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (texture.getWidth() != count * 100 || texture.getHeight() != 100) {
            texture.dispose();
            throw new IllegalArgumentException("Unexpected Werebear sheet dimensions: " + path);
        }
        Animation<TextureRegion> animation = new Animation<>(duration,
            TextureRegion.split(texture, 100, 100)[0]);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        werebearAnimations.put(name, animation);
        return animation;
    }

    public static void disposeWerebearAnimations() {
        for (Animation<TextureRegion> animation : werebearAnimations.values()) {
            animation.getKeyFrames()[0].getTexture().dispose();
        }
        werebearAnimations.clear();
    }

    public static Entity createBoss(World world, Vector2 spawn) {
        Entity boss = createBase(world, spawn, 0.8f);
        boss.add(new BossComponent());
        HealthComponent health = boss.getComponent(HealthComponent.class);
        health.maximum = 400f;
        health.current = health.maximum;
        EnemyAIComponent ai = boss.getComponent(EnemyAIComponent.class);
        ai.detectionRange = 30f;
        ai.attackRange = 1.7f;
        ai.movementSpeed = 1.7f;
        ai.attackWindup = 0.75f;
        ai.recoveryDuration = 0.9f;
        ai.attackDamage = 20f;
        return boss;
    }

    public static Entity createCharger(World world, Vector2 spawn) {
        Entity charger = createBase(world, spawn, 0.5f);
        charger.add(new ChargerComponent());
        return charger;
    }

    public static Entity createWitch(World world, Vector2 spawn) {
        Entity witch = createBase(world, spawn, WitchComponent.BODY_RADIUS);
        witch.add(new WitchComponent());
        addWitchAnimations(witch);
        witch.getComponent(EnemyAIComponent.class).detectionRange = WitchComponent.DETECTION_RANGE;
        // Continuous collision for the fast rush; PhysicsSystem still owns movement.
        WorldUtils.configureWitchBody(witch.getComponent(PhysicsComponent.class).body);
        return witch;
    }

    private static void addWitchAnimations(Entity enemy) {
        AnimationComponent animation = new AnimationComponent();
        animation.idle = witchAnimation("idle", 32, 48, 6, 0.12f, true);
        animation.walk = witchAnimation("run", 32, 48, 8, 0.09f, true);
        animation.charge = witchAnimation("charge", 48, 48, 5,
            WitchComponent.CHARGE_ANIMATION_FRAME_DURATION, true);
        animation.attack = witchAnimation("attack", 104, 46,
            WitchComponent.ATTACK_ANIMATION_FRAME_COUNT,
            WitchComponent.ATTACK_ANIMATION_FRAME_DURATION, false);
        animation.hurt = witchAnimation("take_damage", 32, 48, 3, 0.08f, false);
        animation.death = witchAnimation("death", 32, 48, 10, 0.10f, false);
        // One scale for every sheet: extra attack width belongs to the spell.
        animation.renderPixelScale = 0.037f;
        animation.bodyAnchorX = 16f;
        animation.chargeBodyAnchorX = 24f;
        animation.attackBodyAnchorX = 20f;
        // Body center is 22 source pixels above the bottom in both 48/46px sheets.
        animation.renderYOffset = 22f * animation.renderPixelScale;
        animation.previousHealth = enemy.getComponent(HealthComponent.class).current;
        enemy.add(animation);
    }

    private static Animation<TextureRegion> witchAnimation(String name, int width,
        int height, int count, float duration, boolean loop) {
        Animation<TextureRegion> cached = witchAnimations.get(name);
        if (cached != null) return cached;
        Texture texture = new Texture("characters/b_witch/B_witch_" + name + ".png");
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new TextureRegion(texture, 0, i * height, width, height);
        }
        Animation<TextureRegion> animation = new Animation<>(duration, frames);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        witchAnimations.put(name, animation);
        return animation;
    }

    /** Screen-lifetime resources; removing an individual witch never disposes these. */
    public static void disposeWitchAnimations() {
        for (Animation<TextureRegion> animation : witchAnimations.values()) {
            animation.getKeyFrames()[0].getTexture().dispose();
        }
        witchAnimations.clear();
    }

    public static Entity createNecromancer(World world, Vector2 spawn) {
        Entity necromancer = createRangedBase(world, spawn);
        necromancer.add(new NecromancerComponent());
        addNecromancerAnimations(necromancer);
        RangedEnemyComponent ranged = necromancer.getComponent(RangedEnemyComponent.class);
        ranged.preferredMinimumRange = 4f;
        ranged.preferredMaximumRange = 6f;
        ranged.attackCooldown = 2f;
        return necromancer;
    }

    private static void addNecromancerAnimations(Entity enemy) {
        Texture sheet =
            new Texture("characters/Necromancer_creativekind-Sheet.png");
        sheet.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        // Fixed 17-column x 7-row grid; every cell is exactly 160x128.
        TextureRegion[][] rows = TextureRegion.split(sheet, 160, 128);
        AnimationComponent animation = new AnimationComponent();
        animation.sourceTexture = sheet;
        animation.idle = createNecromancerAnimation(rows[0], 8, 0.12f);
        animation.walk = createNecromancerAnimation(rows[1], 8, 0.10f);
        animation.attack = createNecromancerAnimation(rows[2], 13, 0.15f);
        animation.hurt = createNecromancerAnimation(rows[5], 5, 0.025f);
        animation.death = createNecromancerAnimation(rows[6], 9, 0.10f);

        animation.idle.setPlayMode(Animation.PlayMode.LOOP);
        animation.walk.setPlayMode(Animation.PlayMode.LOOP);
        animation.attack.setPlayMode(Animation.PlayMode.NORMAL);
        animation.hurt.setPlayMode(Animation.PlayMode.NORMAL);
        animation.death.setPlayMode(Animation.PlayMode.NORMAL);

        // Transparent padding keeps the visible character near enemy scale.
        animation.renderWidth = 3.4f;
        animation.renderHeight = 2.72f;
        animation.renderYOffset = 0.7f;
        animation.hoverAmplitude = 0.04f;
        animation.sourceFacesLeft = true;
        enemy.add(animation);
    }

    private static Animation<TextureRegion> createNecromancerAnimation(
        TextureRegion[] row,
        int frameCount,
        float frameDuration
    ) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        System.arraycopy(row, 0, frames, 0, frameCount);
        return new Animation<>(frameDuration, frames);
    }

    public static Entity createShieldGuard(World world, Vector2 spawn) {
        Entity guard = createBase(world, spawn, 0.5f);
        guard.add(new ShieldGuardComponent());
        addShieldGuardAnimations(guard);
        return guard;
    }

    @SuppressWarnings("unchecked")
    private static void addShieldGuardAnimations(Entity guard) {
        AnimationComponent animation = new AnimationComponent();
        animation.idle = armoredOrcAnimation("Idle", 6, SHIELD_GUARD_IDLE_FRAME_DURATION, true);
        animation.walk = armoredOrcAnimation("Walk", 8, SHIELD_GUARD_WALK_FRAME_DURATION, true);
        animation.block = armoredOrcAnimation("Block", 4, SHIELD_GUARD_REACTION_FRAME_DURATION, false);
        animation.hurt = armoredOrcAnimation("Hit", 5, SHIELD_GUARD_REACTION_FRAME_DURATION, false);
        animation.death = armoredOrcAnimation("Death", 4, SHIELD_GUARD_DEATH_FRAME_DURATION, false);
        animation.attackVariants = (Animation<TextureRegion>[]) new Animation<?>[] {
            armoredOrcAnimation("Attack01", 7, SHIELD_GUARD_ATTACK_FRAME_DURATION, false),
            armoredOrcAnimation("Attack02", 8, SHIELD_GUARD_ATTACK_FRAME_DURATION, false),
            armoredOrcAnimation("Attack03", 9, SHIELD_GUARD_ATTACK_FRAME_DURATION, false)
        };
        animation.attack = animation.attackVariants[0];
        animation.renderWidth = SHIELD_GUARD_RENDER_SIZE;
        animation.renderHeight = SHIELD_GUARD_RENDER_SIZE;
        animation.renderYOffset = SHIELD_GUARD_RENDER_Y_OFFSET;
        animation.sourceFacesLeft = false;
        animation.previousHealth = guard.getComponent(HealthComponent.class).current;
        guard.add(animation);
    }

    private static Animation<TextureRegion> armoredOrcAnimation(String name, int count,
        float frameDuration, boolean loop) {
        Animation<TextureRegion> cached = armoredOrcAnimations.get(name);
        if (cached != null) return cached;
        String prefix = "characters/armored_orc/Armored Orc_" + name;
        // This checkout uses plain names; also accept the supplied (1) filenames.
        String path = com.badlogic.gdx.Gdx.files.internal(prefix + "(1).png").exists()
            ? prefix + "(1).png" : prefix + ".png";
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[] frames = TextureRegion.split(texture, 100, 100)[0];
        if (frames.length != count || texture.getHeight() != 100) {
            texture.dispose();
            throw new IllegalArgumentException("Unexpected Armored Orc sheet dimensions: " + path);
        }
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        armoredOrcAnimations.put(name, animation);
        return animation;
    }

    public static void disposeShieldGuardAnimations() {
        for (Animation<TextureRegion> animation : armoredOrcAnimations.values()) {
            animation.getKeyFrames()[0].getTexture().dispose();
        }
        armoredOrcAnimations.clear();
    }
}
