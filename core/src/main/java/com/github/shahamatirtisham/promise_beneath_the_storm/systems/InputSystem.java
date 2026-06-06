package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

public class InputSystem extends IteratingSystem {

    private static final Family family = Family.all(
        PlayerComponent.class,
        VelocityComponent.class
    ).get();

    public InputSystem() {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        float speed = 8f;
        velocity.vx = 0;
        velocity.vy = 0;

        // SWAPPED DIRECTIONS
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.vy = -speed;   // W = UP (fixed: was +, now -)
            System.out.println("W pressed - moving UP");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.vy = speed;    // S = DOWN (fixed: was -, now +)
            System.out.println("S pressed - moving DOWN");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.vx = speed;    // A = LEFT (fixed: was -, now +)
            System.out.println("A pressed - moving LEFT");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.vx = -speed;   // D = RIGHT (fixed: was +, now -)
            System.out.println("D pressed - moving RIGHT");
        }

        // Normalize diagonal movement
        if (velocity.vx != 0 && velocity.vy != 0) {
            velocity.vx *= 0.707f;
            velocity.vy *= 0.707f;
        }
    }
}
