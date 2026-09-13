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
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ResurrectionComponent;
import java.util.function.Consumer;

/** Disables defeated enemies while leaving their entity available for death rendering. */
public class DeathSystem extends IteratingSystem {
    private final Entity player;
    private final Consumer<Entity> onEnemyDefeated;

    public DeathSystem(Entity player, Consumer<Entity> onEnemyDefeated) {
        super(Family.all(
            EnemyComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class,
            PhysicsComponent.class,
            VelocityComponent.class
        ).get());
        this.player = player;
        this.onEnemyDefeated = onEnemyDefeated;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        HealthComponent health = enemy.getComponent(HealthComponent.class);
        EnemyAIComponent ai = enemy.getComponent(EnemyAIComponent.class);
        if (health.current > 0f || ai.state == EnemyAIComponent.State.DEAD) {
            return;
        }

        ai.state = EnemyAIComponent.State.DEAD;
        VelocityComponent velocity = enemy.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        enemy.getComponent(PhysicsComponent.class).body.setActive(false);

        ResurrectionComponent resurrection =
            enemy.getComponent(ResurrectionComponent.class);
        if (resurrection != null && !resurrection.hasResurrected) {
            resurrection.awaitingResurrection = true;
            return;
        }
        player.getComponent(RunInventoryComponent.class).enemiesDefeated++;
        onEnemyDefeated.accept(enemy);
    }
}
