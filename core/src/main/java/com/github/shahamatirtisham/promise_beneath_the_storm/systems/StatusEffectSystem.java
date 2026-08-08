package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;

/** Advances status timers and applies one combined damage-over-time tick per second. */
public class StatusEffectSystem extends IteratingSystem {
    public StatusEffectSystem() {
        super(Family.all(HealthComponent.class, StatusEffectComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatusEffectComponent status = entity.getComponent(StatusEffectComponent.class);
        HealthComponent health = entity.getComponent(HealthComponent.class);
        PlayerComponent player = entity.getComponent(PlayerComponent.class);
        boolean debugGodMode = player != null && player.debugGodMode;
        boolean takesDamage = !debugGodMode
            && (status.burningTime > 0f || status.poisonTime > 0f);

        status.burningTime = Math.max(0f, status.burningTime - deltaTime);
        status.poisonTime = Math.max(0f, status.poisonTime - deltaTime);
        status.slowTime = Math.max(0f, status.slowTime - deltaTime);
        status.stunTime = Math.max(0f, status.stunTime - deltaTime);

        if (!takesDamage || health.current <= 0f) {
            status.damageTickTime = 0f;
            return;
        }

        status.damageTickTime -= deltaTime;
        if (status.damageTickTime <= 0f) {
            float damage = (status.burningTime > 0f ? 4f : 0f)
                + (status.poisonTime > 0f ? 2f : 0f);
            health.current = Math.max(0f, health.current - damage);
            status.damageTickTime = 1f;
        }
    }
}
