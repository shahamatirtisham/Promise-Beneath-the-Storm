package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;

/** Tracks right-mouse blocking and its short perfect-parry window. */
public class DefenseSystem extends IteratingSystem {
    public DefenseSystem() {
        super(Family.all(PlayerComponent.class, DefenseComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = entity.getComponent(PlayerComponent.class);
        DefenseComponent defense = entity.getComponent(DefenseComponent.class);
        defense.feedbackTimeRemaining = Math.max(
            0f,
            defense.feedbackTimeRemaining - deltaTime
        );

        if (player.dead) {
            defense.blocking = false;
            defense.parryTimeRemaining = 0f;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            defense.blocking = true;
            defense.parryTimeRemaining = defense.parryWindow;
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            defense.blocking = false;
            defense.parryTimeRemaining = 0f;
            return;
        }

        defense.parryTimeRemaining = Math.max(
            0f,
            defense.parryTimeRemaining - deltaTime
        );
    }
}
