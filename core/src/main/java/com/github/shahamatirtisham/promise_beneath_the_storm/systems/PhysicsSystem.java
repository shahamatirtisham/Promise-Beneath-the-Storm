package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

/** Lets Box2D own movement and mirrors the resulting position back into ECS. */
public class PhysicsSystem extends IteratingSystem {
    private static final float TIME_STEP = 1f / 60f;
    private static final float MAX_FRAME_TIME = 0.25f;

    private final World world;
    private float accumulator;

    public PhysicsSystem(World world) {
        super(Family.all(PositionComponent.class, VelocityComponent.class, PhysicsComponent.class).get());
        this.world = world;
    }

    @Override
    public void update(float deltaTime) {
        float frameTime = Math.min(deltaTime, MAX_FRAME_TIME);

        // Apply the latest input before advancing the physics world.
        for (Entity entity : getEntities()) {
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            physics.body.setLinearVelocity(velocity.vx, velocity.vy);
        }

        accumulator += frameTime;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }

        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = entity.getComponent(PositionComponent.class);
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        position.x = physics.body.getPosition().x;
        position.y = physics.body.getPosition().y;
    }
}
