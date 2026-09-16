package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;

/** Screen-lifetime cache for the supplied 100x100 Iron Fist FX strips. */
public final class IronFistFxResources implements Disposable {
    private static final String ROOT = "characters/irhos/iron_fist_fx/";
    public static final float CIRCLE_FRAME_DURATION = 0.12f;
    public static final float WIZARD_FRAME_DURATION = 0.08f;
    public static final float SKELETON_FRAME_DURATION = 0.12f;
    private final List<Texture> textures = new ArrayList<>();

    public Animation<TextureRegion> summonCircle;
    public Animation<TextureRegion> wizardStrike;
    public Animation<TextureRegion> skeletonIdle;
    public Animation<TextureRegion> skeletonWalk;
    public Animation<TextureRegion> skeletonAttack01;
    public Animation<TextureRegion> skeletonAttack02;
    public Animation<TextureRegion> skeletonHurt;
    public Animation<TextureRegion> skeletonBlock;
    public Animation<TextureRegion> skeletonDeath;
    public Animation<TextureRegion> skeletonSummon;

    public void load() {
        if (summonCircle != null) return;
        summonCircle = load("Necromancer_Sumon_Effect.png", 7, CIRCLE_FRAME_DURATION, false);
        wizardStrike = load("Wizard_Attack01_Effect.png", 10, WIZARD_FRAME_DURATION, false);
        skeletonIdle = load("Skeleton_Idle.png", 6, 0.16f, true);
        skeletonWalk = load("Skeleton_Walk.png", 8, 0.11f, true);
        skeletonAttack01 = load("Skeleton_Attack01.png", 6, SKELETON_FRAME_DURATION, false);
        skeletonAttack02 = load("Skeleton_Attack02.png", 7, SKELETON_FRAME_DURATION, false);
        skeletonHurt = load("Skeleton_Hurt.png", 4, SKELETON_FRAME_DURATION, false);
        skeletonBlock = load("Skeleton_Block.png", 4, SKELETON_FRAME_DURATION, false);
        skeletonDeath = load("Skeleton_Death.png", 4, SKELETON_FRAME_DURATION, false);
        skeletonSummon = load("Skeleton_Summon.png", 5, SKELETON_FRAME_DURATION, false);
    }

    private Animation<TextureRegion> load(String file, int frames, float duration, boolean loop) {
        Texture texture = new Texture(Gdx.files.internal(ROOT + file));
        if (texture.getWidth() != frames * 100 || texture.getHeight() != 100) {
            texture.dispose();
            throw new IllegalArgumentException("Unexpected Iron Fist FX dimensions: " + file);
        }
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        Animation<TextureRegion> animation = new Animation<>(duration,
            TextureRegion.split(texture, 100, 100)[0]);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return animation;
    }

    @Override public void dispose() {
        for (Texture texture : textures) texture.dispose();
        textures.clear();
        summonCircle = null;
        wizardStrike = null;
        skeletonIdle = null;
        skeletonWalk = null;
        skeletonAttack01 = null;
        skeletonAttack02 = null;
        skeletonHurt = null;
        skeletonBlock = null;
        skeletonDeath = null;
        skeletonSummon = null;
    }
}
