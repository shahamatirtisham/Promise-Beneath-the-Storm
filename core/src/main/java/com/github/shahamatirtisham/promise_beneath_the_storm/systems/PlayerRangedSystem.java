package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.ProjectileFactory;

/** Consumes an inventory knife and throws it toward the player's mouse aim on Q. */
public class PlayerRangedSystem extends IteratingSystem {
    private final Engine engine;
    private final Array<Entity> projectiles;

    public PlayerRangedSystem(Engine engine, Array<Entity> projectiles) {
        super(Family.all(
            PlayerComponent.class,
            PlayerRangedComponent.class,
            PositionComponent.class,
            FacingComponent.class
        ).get());
        this.engine = engine;
        this.projectiles = projectiles;
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        PlayerRangedComponent ranged = player.getComponent(PlayerRangedComponent.class);
        ranged.cooldownRemaining = Math.max(0f, ranged.cooldownRemaining - deltaTime);

        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead || playerState.controlsLocked
            || player.getComponent(StatusEffectComponent.class).isStunned()
            || player.getComponent(DefenseComponent.class).blocking
            || player.getComponent(AttackComponent.class).isActive()
            || ranged.charges <= 0
            || ranged.cooldownRemaining > 0f
            || !Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            return;
        }

        PositionComponent position = player.getComponent(PositionComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        Entity knife = ProjectileFactory.createPlayerKnife(
            position.x + facing.x * 0.55f,
            position.y + facing.y * 0.55f,
            facing.x,
            facing.y,
            ranged.projectileSpeed,
            ranged.damage
        );
        projectiles.add(knife);
        engine.addEntity(knife);
        ranged.charges--;
        ranged.cooldownRemaining = ranged.cooldownDuration;
        Gdx.app.log("Combat", "Knife thrown. Remaining: " + ranged.charges);
    }
}
