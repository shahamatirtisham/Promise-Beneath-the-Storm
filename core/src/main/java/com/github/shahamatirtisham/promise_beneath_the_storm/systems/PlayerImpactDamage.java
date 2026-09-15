package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;

/** Shared projectile-style defense resolution; returns true only when health is lost. */
public final class PlayerImpactDamage {
    private PlayerImpactDamage() { }
    public static boolean apply(Entity player, float sourceX, float sourceY, float baseDamage, int statusLevel) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        InvulnerabilityComponent immunity = player.getComponent(InvulnerabilityComponent.class);
        PlayerComponent state = player.getComponent(PlayerComponent.class);
        if (state.dead || health.current <= 0f || immunity.isActive()) return false;
        PositionComponent position = player.getComponent(PositionComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        DefenseComponent defense = player.getComponent(DefenseComponent.class);
        float dx = sourceX - position.x, dy = sourceY - position.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        boolean faces = length == 0f || (facing.x * dx + facing.y * dy) / length >= 0.2f;
        if (faces && defense.isParryActive()) {
            defense.feedbackTimeRemaining = 0.25f;
            PlayerAnimationComponent animation = player.getComponent(PlayerAnimationComponent.class);
            if (animation != null) animation.requestParryAnimation(facing.x, facing.y);
            Gdx.app.log("Combat", "Perfect parry - projectile destroyed");
            return false;
        }
        float damage = baseDamage;
        if (faces && defense.blocking) {
            damage *= 1f - defense.damageReduction;
            Gdx.app.log("Combat", "Blocked projectile damage: " + (int) damage);
        }
        float before = health.current;
        health.current = Math.max(0f, health.current - damage);
        immunity.timeRemaining = immunity.duration;
        if (!faces || !defense.blocking) StatusEffectApplicator.applyForLevel(player, statusLevel);
        return health.current < before;
    }
}
