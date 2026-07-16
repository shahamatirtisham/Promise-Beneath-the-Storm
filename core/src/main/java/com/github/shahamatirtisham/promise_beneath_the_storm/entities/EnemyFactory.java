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
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

/** Creates the current placeholder melee-enemy archetype. */
public final class EnemyFactory {
    private static final float ENEMY_RADIUS = 0.45f;

    private EnemyFactory() {
    }

    public static Entity createMelee(World world, Vector2 spawn) {
        Body body = WorldUtils.createDynamicCircle(
            world,
            spawn.x,
            spawn.y,
            ENEMY_RADIUS
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
}
