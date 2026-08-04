package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ProjectileComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Builds lightweight projectiles that do not participate in Box2D movement. */
public final class ProjectileFactory {
    private static final float RADIUS = 0.18f;
    private static final float LIFETIME = 2.5f;

    private ProjectileFactory() {
    }

    public static Entity createEnemyProjectile(
        float x,
        float y,
        float directionX,
        float directionY,
        float speed,
        float damage
    ) {
        return createEnemyProjectile(
            x,
            y,
            directionX,
            directionY,
            speed,
            damage,
            0
        );
    }

    public static Entity createEnemyProjectile(
        float x,
        float y,
        float directionX,
        float directionY,
        float speed,
        float damage,
        int statusLevelOverride
    ) {
        Entity projectile = new Entity();
        projectile.add(new PositionComponent(x, y));
        projectile.add(new VelocityComponent(directionX * speed, directionY * speed));
        projectile.add(new ProjectileComponent(
            damage,
            LIFETIME,
            RADIUS,
            statusLevelOverride
        ));
        projectile.add(new TeamComponent(TeamComponent.Team.ENEMY));
        return projectile;
    }

    public static Entity createPlayerKnife(
        float x,
        float y,
        float directionX,
        float directionY,
        float speed,
        float damage
    ) {
        Entity projectile = new Entity();
        projectile.add(new PositionComponent(x, y));
        projectile.add(new VelocityComponent(directionX * speed, directionY * speed));
        projectile.add(new ProjectileComponent(damage, 1.5f, 0.14f));
        projectile.add(new TeamComponent(TeamComponent.Team.PLAYER));
        return projectile;
    }
}
