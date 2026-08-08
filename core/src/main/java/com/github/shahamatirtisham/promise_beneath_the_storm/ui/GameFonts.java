package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/** Creates consistently rendered Cinzel fonts from the bundled game asset. */
public final class GameFonts {
    private static final String CINZEL_PATH = "fonts/Cinzel.ttf";

    private GameFonts() {}

    public static BitmapFont create(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal(CINZEL_PATH)
        );
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameters =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameters.size = size;
            parameters.color = Color.WHITE;
            parameters.borderWidth = 1f;
            parameters.borderColor = new Color(0.025f, 0.02f, 0.015f, 0.9f);
            parameters.minFilter = Texture.TextureFilter.Linear;
            parameters.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameters);
        } finally {
            generator.dispose();
        }
    }
}
