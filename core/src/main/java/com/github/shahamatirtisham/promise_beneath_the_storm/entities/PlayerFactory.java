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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
/** Creates a complete player entity with gameplay and physics components. */
public final class PlayerFactory {
    private static final float PLAYER_RADIUS = 0.4f;
    private static final float BLOCK_FRAME_DURATION = 0.08f;
    private static final float PARRY_FRAME_DURATION = 0.06f;

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

        // The final row contains three death frames followed by three empty cells.
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> deathAnimation =
            AnimationFactory.createPlayerAnimation(
                "characters/player.png",
                9,
                3,
                0.18f
            );
        animation.deathDown = deathAnimation;
        animation.deathSide = deathAnimation;
        animation.deathUp = deathAnimation;

        animation.blockTexture = new Texture("characters/player_block.png");
        animation.blockTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );
        TextureRegion[][] blockRows = TextureRegion.split(
            animation.blockTexture,
            256,
            256
        );
        animation.blockDown = createBlockAnimation(blockRows[0]);
        animation.blockLeft = createBlockAnimation(blockRows[1]);
        animation.blockUp = createBlockAnimation(blockRows[2]);
        animation.blockRight = createBlockAnimation(blockRows[3]);

        animation.parryTexture =
            new Texture("characters/player_parry_game_ready.png");
        animation.parryTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );
        TextureRegion[][] parryRows = TextureRegion.split(
            animation.parryTexture,
            256,
            256
        );
        // The parry sheet rows are DOWN, RIGHT, UP, LEFT.
        animation.parryDown = createParryAnimation(parryRows[0]);
        animation.parryRight = createParryAnimation(parryRows[1]);
        animation.parryUp = createParryAnimation(parryRows[2]);
        animation.parryLeft = createParryAnimation(parryRows[3]);

        animation.attackDown.setPlayMode(Animation.PlayMode.NORMAL);
        animation.attackSide.setPlayMode(Animation.PlayMode.NORMAL);
        animation.attackUp.setPlayMode(Animation.PlayMode.NORMAL);
        deathAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        animation.blockDown.setPlayMode(Animation.PlayMode.NORMAL);
        animation.blockLeft.setPlayMode(Animation.PlayMode.NORMAL);
        animation.blockRight.setPlayMode(Animation.PlayMode.NORMAL);
        animation.blockUp.setPlayMode(Animation.PlayMode.NORMAL);
        animation.parryDown.setPlayMode(Animation.PlayMode.NORMAL);
        animation.parryLeft.setPlayMode(Animation.PlayMode.NORMAL);
        animation.parryRight.setPlayMode(Animation.PlayMode.NORMAL);
        animation.parryUp.setPlayMode(Animation.PlayMode.NORMAL);

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

    private static Animation<TextureRegion> createBlockAnimation(
        TextureRegion[] row
    ) {
        return new Animation<>(
            BLOCK_FRAME_DURATION,
            row[0],
            row[1],
            row[2]
        );
    }

    private static Animation<TextureRegion> createParryAnimation(
        TextureRegion[] row
    ) {
        return new Animation<>(
            PARRY_FRAME_DURATION,
            row[0],
            row[1],
            row[2],
            row[3]
        );
    }
}
