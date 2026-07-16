package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;

/** Disables defeated enemies while leaving their entity available for death rendering. */
public class DeathSystem extends IteratingSystem {
    private final Entity player;

    public DeathSystem(Entity player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class,
            PhysicsComponent.class,
            VelocityComponent.class
        ).get());
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        HealthComponent health = enemy.getComponent(HealthComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        if (health.current > 0f || ai.state == EnemyAIComponent.State.DEAD) {
            return;
        }

        ai.state = EnemyAIComponent.State.DEAD;
        player.getComponent(RunInventoryComponent.class).enemiesDefeated++;
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        enemy.getComponent(PhysicsComponent.class).body.setActive(false);
    }
}
