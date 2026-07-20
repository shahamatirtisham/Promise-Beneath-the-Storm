package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.TeamComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;

/** Creates a complete player entity with gameplay and physics components. */
public final class PlayerFactory {
    private static final float PLAYER_RADIUS = 0.4f;

    private PlayerFactory() {
    }

    public static Entity create(World world, Vector2 spawn) {
        Body body = WorldUtils.createDynamicCircle(
            world,
            spawn.x,
            spawn.y,
            PLAYER_RADIUS
        );

        Entity player = new Entity();
        player.add(new PlayerComponent());
        player.add(new PositionComponent(spawn.x, spawn.y));
        player.add(new VelocityComponent());
        player.add(new PhysicsComponent(body));
        player.add(new FacingComponent());
        player.add(new HealthComponent(100f));
        player.add(new InvulnerabilityComponent());
        player.add(new TeamComponent(TeamComponent.Team.PLAYER));
        player.add(new AttackComponent());
        player.add(new RunInventoryComponent());
        player.add(new RelicInventoryComponent());
        player.add(new DashComponent());
        player.add(new DefenseComponent());
        return player;
    }
}
