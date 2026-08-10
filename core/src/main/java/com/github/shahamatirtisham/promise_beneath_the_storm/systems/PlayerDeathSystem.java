package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Enters the player death state when health reaches zero. */
public class PlayerDeathSystem extends IteratingSystem {
    public PlayerDeathSystem() {
        super(Family.all(
            PlayerComponent.class,
            HealthComponent.class,
            VelocityComponent.class,
            AttackComponent.class,
            PlayerAnimationComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = entity.getComponent(PlayerComponent.class);
        HealthComponent health = entity.getComponent(HealthComponent.class);
        if (player.dead || health.current > 0f) {
            return;
        }

        player.dead = true;
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        entity.getComponent(AttackComponent.class).activeTimeRemaining = 0f;
        PlayerAnimationComponent animation =
            entity.getComponent(PlayerAnimationComponent.class);
        animation.state = PlayerAnimationComponent.State.DEAD;
        animation.stateTime = 0f;
        Gdx.app.log("Player", "You died. Press R to restore the latest checkpoint.");
    }
}
