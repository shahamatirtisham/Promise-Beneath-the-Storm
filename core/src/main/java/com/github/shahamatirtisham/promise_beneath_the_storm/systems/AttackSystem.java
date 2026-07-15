package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;

/** Starts and advances the player's temporary melee attack window. */
public class AttackSystem extends IteratingSystem {
    public AttackSystem() {
        super(Family.all(
            PlayerComponent.class,
            FacingComponent.class,
            AttackComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AttackComponent attack = entity.getComponent(AttackComponent.class);
        attack.activeTimeRemaining = Math.max(0f, attack.activeTimeRemaining - deltaTime);
        attack.cooldownRemaining = Math.max(0f, attack.cooldownRemaining - deltaTime);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            && attack.cooldownRemaining <= 0f) {
            attack.activeTimeRemaining = attack.activeDuration;
            attack.cooldownRemaining = attack.cooldownDuration;
            attack.attackId++;
        }
    }
}
