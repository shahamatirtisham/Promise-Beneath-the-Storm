package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.LeverComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

public final class LeverFactory {
    /** The last tileset row contains five individual 16-pixel lever poses. */
    public static com.badlogic.gdx.graphics.g2d.TextureRegion[] createFrames(
        com.badlogic.gdx.graphics.Texture texture
    ) {
        com.badlogic.gdx.graphics.g2d.TextureRegion[] frames =
            new com.badlogic.gdx.graphics.g2d.TextureRegion[LeverComponent.FRAME_COUNT];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new com.badlogic.gdx.graphics.g2d.TextureRegion(texture, i * 16, 176, 16, 16);
        }
        return frames;
    }

    private LeverFactory() {
    }

    public static Entity create(Vector2 position) {
        Entity lever = new Entity();
        lever.add(new PositionComponent(position.x, position.y));
        lever.add(new LeverComponent());
        return lever;
    }
}
