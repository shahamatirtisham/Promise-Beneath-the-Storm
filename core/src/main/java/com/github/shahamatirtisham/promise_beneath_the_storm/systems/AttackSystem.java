package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;

/** Starts and advances the player's temporary melee attack window. */
public class AttackSystem extends IteratingSystem {
    public AttackSystem() {
        super(Family.all(
            PlayerComponent.class,
            FacingComponent.class,
            AttackComponent.class,
            DefenseComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AttackComponent attack = entity.getComponent(AttackComponent.class);
        attack.activeTimeRemaining = Math.max(0f, attack.activeTimeRemaining - deltaTime);
        attack.cooldownRemaining = Math.max(0f, attack.cooldownRemaining - deltaTime);
        attack.comboResetRemaining = Math.max(0f, attack.comboResetRemaining - deltaTime);

        if (attack.comboResetRemaining <= 0f) {
            attack.comboStep = -1;
        }

        if (entity.getComponent(PlayerComponent.class).dead
            || entity.getComponent(DefenseComponent.class).blocking) {
            attack.activeTimeRemaining = 0f;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            && attack.cooldownRemaining <= 0f) {
            beginNextComboAttack(attack);
        }
    }

    private void beginNextComboAttack(AttackComponent attack) {
        attack.comboStep = attack.comboStep >= 0 && attack.comboStep < 2
            ? attack.comboStep + 1
            : 0;

        switch (attack.comboStep) {
            case 0:
                attack.damage = 15f;
                attack.activeDuration = 0.12f;
                attack.cooldownDuration = 0.18f;
                attack.reach = 1.25f;
                attack.halfWidth = 0.38f;
                attack.knockbackStrength = 0f;
                break;
            case 1:
                attack.damage = 20f;
                attack.activeDuration = 0.14f;
                attack.cooldownDuration = 0.2f;
                attack.reach = 1.35f;
                attack.halfWidth = 0.42f;
                attack.knockbackStrength = 0f;
                break;
            case 2:
                attack.damage = 30f;
                attack.activeDuration = 0.2f;
                attack.cooldownDuration = 0.3f;
                attack.reach = 1.5f;
                attack.halfWidth = 0.5f;
                attack.knockbackStrength = 2.8f;
                break;
        }

        attack.activeTimeRemaining = attack.activeDuration;
        attack.cooldownRemaining = attack.cooldownDuration;
        attack.comboResetRemaining = attack.comboResetDuration;
        attack.attackId++;
        Gdx.app.log("Combat", "Combo hit " + (attack.comboStep + 1));
    }
}
