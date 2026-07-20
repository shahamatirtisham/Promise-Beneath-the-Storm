package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

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
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

/** Creates the current placeholder melee-enemy archetype. */
public final class EnemyFactory {
    private static final float ENEMY_RADIUS = 0.45f;

    private EnemyFactory() {
    }

    public static Entity createMelee(World world, Vector2 spawn) {
        return createBase(world, spawn, ENEMY_RADIUS);
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
        Entity enemy = createMelee(world, spawn);
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
}
