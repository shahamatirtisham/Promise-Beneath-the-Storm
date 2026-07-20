package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;

/** Overrides normal movement during a short invulnerable dash. */
public class DashSystem extends IteratingSystem {
    public DashSystem() {
        super(Family.all(
            PlayerComponent.class,
            VelocityComponent.class,
            FacingComponent.class,
            DashComponent.class,
            InvulnerabilityComponent.class,
            DefenseComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = entity.getComponent(PlayerComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        DashComponent dash = entity.getComponent(DashComponent.class);

        dash.activeTimeRemaining = Math.max(0f, dash.activeTimeRemaining - deltaTime);
        dash.cooldownRemaining = Math.max(0f, dash.cooldownRemaining - deltaTime);

        if (player.dead || player.controlsLocked
            || entity.getComponent(StatusEffectComponent.class).isStunned()) {
            dash.activeTimeRemaining = 0f;
            return;
        }

        if (entity.getComponent(DefenseComponent.class).blocking) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            && dash.cooldownRemaining <= 0f) {
            beginDash(entity, velocity, dash);
        }

        if (!dash.isActive()) {
            return;
        }

        velocity.vx = dash.directionX * dash.speed;
        velocity.vy = dash.directionY * dash.speed;
        InvulnerabilityComponent invulnerability =
            entity.getComponent(InvulnerabilityComponent.class);
        invulnerability.timeRemaining = Math.max(
            invulnerability.timeRemaining,
            dash.activeTimeRemaining
        );
    }

    private void beginDash(
        Entity entity,
        VelocityComponent velocity,
        DashComponent dash
    ) {
        float lengthSquared = velocity.vx * velocity.vx + velocity.vy * velocity.vy;
        if (lengthSquared > 0f) {
            float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
            dash.directionX = velocity.vx * inverseLength;
            dash.directionY = velocity.vy * inverseLength;
        } else {
            FacingComponent facing = entity.getComponent(FacingComponent.class);
            dash.directionX = facing.x;
            dash.directionY = facing.y;
        }

        dash.activeTimeRemaining = dash.activeDuration;
        dash.cooldownRemaining = dash.cooldownDuration;
    }
}
