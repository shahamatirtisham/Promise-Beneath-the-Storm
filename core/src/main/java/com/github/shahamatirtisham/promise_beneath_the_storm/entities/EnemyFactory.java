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
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

/** Creates the current placeholder melee-enemy archetype. */
public final class EnemyFactory {
    private static final float ENEMY_RADIUS = 0.45f;

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

    public static Entity createRanged(World world, Vector2 spawn) {
        Entity enemy = createBase(world, spawn, ENEMY_RADIUS);
        enemy.add(new RangedEnemyComponent());
        return enemy;
    }

    public static Entity createHeavy(World world, Vector2 spawn) {
        Entity enemy = createBase(world, spawn, 0.65f);
        enemy.add(new HeavyEnemyComponent());
        return enemy;
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

    public static Entity createNecromancer(World world, Vector2 spawn) {
        Entity necromancer = createRanged(world, spawn);
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
        return guard;
    }
}
