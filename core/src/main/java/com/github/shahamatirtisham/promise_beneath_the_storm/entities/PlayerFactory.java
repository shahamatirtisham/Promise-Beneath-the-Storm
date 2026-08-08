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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
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
        player.add(new StatusEffectComponent());
        player.add(new DashComponent());
        player.add(new DefenseComponent());
        PlayerAnimationComponent animation = new PlayerAnimationComponent();

        animation.dust =
            AnimationFactory.createDustAnimation(
                "characters/dust_particles_01.png",
                0.08f
            );

        animation.idleDown =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                0,
                0.18f);

        animation.idleSide =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                1,
                0.18f);

        animation.idleUp =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                2,
                0.18f);

        animation.walkDown =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                3,
                0.12f);

        animation.walkSide =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                4,
                0.12f);

        animation.walkUp =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                5,
                0.12f);

        animation.attackDown =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                6,
                0.08f);

        animation.attackSide =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                7,
                0.08f);

        animation.attackUp =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                8,
                0.08f);

        animation.attackDown.setPlayMode(Animation.PlayMode.NORMAL);
        animation.attackSide.setPlayMode(Animation.PlayMode.NORMAL);
        animation.attackUp.setPlayMode(Animation.PlayMode.NORMAL);

        animation.idleDown.setPlayMode(Animation.PlayMode.LOOP);
        animation.idleSide.setPlayMode(Animation.PlayMode.LOOP);
        animation.idleUp.setPlayMode(Animation.PlayMode.LOOP);

        animation.walkDown.setPlayMode(Animation.PlayMode.LOOP);
        animation.walkSide.setPlayMode(Animation.PlayMode.LOOP);
        animation.walkUp.setPlayMode(Animation.PlayMode.LOOP);

        player.add(animation);
        player.add(new PlayerRangedComponent());
        return player;
    }
}
