package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/** Screen-owned shared resources and centralized bomb tuning. */
public final class BombFactory implements Disposable {
    public static final float BOMB_FRAME_DURATION = 0.08f;
    public static final float EXPLOSION_FRAME_DURATION = 0.07f;
    public static final float BOMB_THROW_RANGE = 5f;
    public static final float BOMB_FLIGHT_DURATION = 0.8f;
    public static final float BOMB_ARC_HEIGHT = 1.5f;
    public static final float BOMB_DAMAGE = 35f;
    public static final float BOMB_EXPLOSION_RADIUS = 2f;
    public static final float BOMB_THROW_COOLDOWN = 0.6f;
    public static final int BOMB_DEBUG_GRANT_AMOUNT = 5;
    public static final float BOMB_RENDER_SIZE = 0.55f;
    public static final float EXPLOSION_RENDER_SIZE = 4.5f;
    public static final float BOMB_GROUND_CLEARANCE = 0.12f;
    private final Texture bombTexture;
    private final Texture explosionTexture;
    public final Animation<TextureRegion> flight;
    public final Animation<TextureRegion> explosion;
    private boolean disposed;

    public BombFactory() {
        bombTexture = new Texture(Gdx.files.internal("throwables/bomb/bomb.png"));
        explosionTexture = new Texture(Gdx.files.internal("throwables/bomb/bomb_ex.png"));
        bombTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        explosionTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[] rotation = new TextureRegion[8];
        for (int i = 0; i < rotation.length; i++) {
            int x = i * 216;
            rotation[i] = new TextureRegion(bombTexture, x, 0,
                Math.min(216, bombTexture.getWidth() - x), 215);
        }
        flight = new Animation<>(BOMB_FRAME_DURATION, rotation);
        flight.setPlayMode(Animation.PlayMode.LOOP);
        // Seven horizontal 48x48 cells; the final blank cell is omitted.
        TextureRegion[] blast = new TextureRegion[6];
        for (int i = 0; i < blast.length; i++) {
            blast[i] = new TextureRegion(explosionTexture, i * 48, 0, 48, 48);
        }
        explosion = new Animation<>(EXPLOSION_FRAME_DURATION, blast);
        explosion.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override public void dispose() {
        if (disposed) return;
        disposed = true;
        bombTexture.dispose();
        explosionTexture.dispose();
    }
}
