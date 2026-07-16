package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.KnockbackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Overrides AI movement briefly so an impact can visibly push an entity. */
public class KnockbackSystem extends IteratingSystem {
    public KnockbackSystem() {
        super(Family.all(KnockbackComponent.class, VelocityComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        KnockbackComponent knockback = entity.getComponent(KnockbackComponent.class);
        if (knockback.timeRemaining <= 0f) {
            return;
        }

        knockback.timeRemaining = Math.max(0f, knockback.timeRemaining - deltaTime);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = knockback.velocityX;
        velocity.vy = knockback.velocityY;
    }
}
