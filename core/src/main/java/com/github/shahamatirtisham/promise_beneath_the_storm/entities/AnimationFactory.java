package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class AnimationFactory {

    private AnimationFactory() {
    }

    /*
     * Existing Orc animation loader.
     */
    public static Animation<TextureRegion> createAnimation(
        String path,
        float frameDuration
    ) {

        Texture texture = new Texture(path);

        TextureRegion[][] split =
            TextureRegion.split(texture, 100, 100);

        TextureRegion[] frames = split[0];

        return new Animation<>(frameDuration, frames);
    }

    /*
     * Mystic Woods player animation loader.
     */
    public static Animation<TextureRegion> createPlayerAnimation(
        String path,
        int row,
        float frameDuration
    ) {

        Texture texture = new Texture(path);

        TextureRegion[][] split =
            TextureRegion.split(texture, 48, 48);

        TextureRegion[] frames =
            new TextureRegion[split[row].length];

        for (int i = 0; i < split[row].length; i++) {

            frames[i] = split[row][i];
        }

        return new Animation<>(
            frameDuration,
            frames
        );
    }

    public static Animation<TextureRegion> createDustAnimation(
        String path,
        float frameDuration
    ) {

        Texture texture = new Texture(path);

        TextureRegion[][] split =
            TextureRegion.split(texture, 12, 12);

        TextureRegion[] frames = split[0];

        return new Animation<>(
            frameDuration,
            frames
        );
    }

    /*
     * Chest animation loader
     *
     * 16x16 frames
     * 1 row
     */
    public static Animation<TextureRegion> createChestAnimation(
        String path,
        float frameDuration
    ) {

        Texture texture = new Texture(path);

        TextureRegion[][] split =
            TextureRegion.split(texture, 16, 16);

        TextureRegion[] frames = split[0];

        return new Animation<>(frameDuration, frames);
    }
}
