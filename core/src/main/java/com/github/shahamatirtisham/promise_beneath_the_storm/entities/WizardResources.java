package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;
import static com.github.shahamatirtisham.promise_beneath_the_storm.components.WizardComponent.*;

/** Shared screen-lifetime sheets; effects and individual deaths never dispose them. */
public final class WizardResources {
    private static final Map<String, Animation<TextureRegion>> sheets = new HashMap<>();
    private static Animation<TextureRegion> flight;
    private static Animation<TextureRegion> impact;
    private static Animation<TextureRegion> wallImpact;
    private WizardResources() { }
    private static Animation<TextureRegion> sheet(String name, int count, float duration, boolean loop) {
        Animation<TextureRegion> cached = sheets.get(name);
        if (cached != null) return cached;
        String path = "characters/wizard/Wizard-" + name + ".png";
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (texture.getWidth() != count * 100 || texture.getHeight() != 100) {
            texture.dispose();
            throw new IllegalArgumentException("Unexpected Wizard sheet dimensions: " + path);
        }
        Animation<TextureRegion> animation = new Animation<>(duration, TextureRegion.split(texture, 100, 100)[0]);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        sheets.put(name, animation);
        return animation;
    }
    public static Animation<TextureRegion> idle() { return sheet("Idle", 6, WIZARD_IDLE_FRAME_DURATION, true); }
    public static Animation<TextureRegion> walk() { return sheet("Walk", 8, WIZARD_WALK_FRAME_DURATION, true); }
    public static Animation<TextureRegion> hurt() { return sheet("Hurt", 4, WIZARD_HURT_FRAME_DURATION, false); }
    public static Animation<TextureRegion> death() { return sheet("DEATH", 4, WIZARD_DEATH_FRAME_DURATION, false); }
    public static Animation<TextureRegion> attack01() { return sheet("Attack01", 6, WIZARD_ATTACK01_FRAME_DURATION, false); }
    public static Animation<TextureRegion> attack02() { return sheet("Attack02", 6, WIZARD_ATTACK02_FRAME_DURATION, false); }
    public static Animation<TextureRegion> crystal() { return sheet("Attack01_Effect", 10, CRYSTAL_EFFECT_FRAME_DURATION, false); }
    private static void fireball() {
        if (flight != null) return;
        TextureRegion[] source = sheet("Attack02_Effect", 7, FIREBALL_EFFECT_FRAME_DURATION, false).getKeyFrames();
        flight = new Animation<>(FIREBALL_EFFECT_FRAME_DURATION, java.util.Arrays.copyOfRange(source, 0, 4));
        flight.setPlayMode(Animation.PlayMode.LOOP);
        impact = new Animation<>(FIREBALL_EFFECT_FRAME_DURATION, java.util.Arrays.copyOfRange(source, 4, 7));
        impact.setPlayMode(Animation.PlayMode.NORMAL);
        wallImpact = new Animation<>(FIREBALL_EFFECT_FRAME_DURATION, java.util.Arrays.copyOfRange(source, 5, 7));
        wallImpact.setPlayMode(Animation.PlayMode.NORMAL);
    }
    public static Animation<TextureRegion> flight() { fireball(); return flight; }
    public static Animation<TextureRegion> impact() { fireball(); return impact; }
    public static Animation<TextureRegion> wallImpact() { fireball(); return wallImpact; }
    public static void dispose() {
        for (Animation<TextureRegion> animation : sheets.values()) animation.getKeyFrames()[0].getTexture().dispose();
        sheets.clear();
        flight = impact = wallImpact = null;
    }
}
