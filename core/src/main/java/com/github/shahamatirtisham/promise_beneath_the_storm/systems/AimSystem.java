package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Converts the mouse position from screen pixels into a world-space facing direction. */
public class AimSystem extends IteratingSystem {
    private static final float MIN_AIM_DISTANCE_SQUARED = 0.0001f;

    private final Viewport viewport;
    private final Vector2 mouseWorld = new Vector2();

    public AimSystem(Viewport viewport) {
        super(Family.all(
            PlayerComponent.class,
            PositionComponent.class,
            FacingComponent.class
        ).get());
        this.viewport = viewport;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = entity.getComponent(PositionComponent.class);
        FacingComponent facing = entity.getComponent(FacingComponent.class);

        mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouseWorld);

        float directionX = mouseWorld.x - position.x;
        float directionY = mouseWorld.y - position.y;
        float lengthSquared = directionX * directionX + directionY * directionY;

        // Preserve the previous direction when the cursor is exactly over the player.
        if (lengthSquared > MIN_AIM_DISTANCE_SQUARED) {
            float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
            facing.x = directionX * inverseLength;
            facing.y = directionY * inverseLength;
        }
    }
}
