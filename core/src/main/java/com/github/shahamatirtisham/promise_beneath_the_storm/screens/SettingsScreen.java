package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class SettingsScreen extends BaseMenuScreen {
    private final Screen returnScreen;

    public SettingsScreen(final Main game, Screen returnScreen) {
        super(game, "SETTINGS");
        this.returnScreen = returnScreen;
        addButton("TOGGLE FULLSCREEN", this::toggleFullscreen);
        addButton("MUSIC: " + percent(game.musicVolume), () -> {
            game.musicVolume = nextVolume(game.musicVolume);
            game.showSettings(returnScreen);
        });
        addButton("SOUND: " + percent(game.soundVolume), () -> {
            game.soundVolume = nextVolume(game.soundVolume);
            game.showSettings(returnScreen);
        });
        addButton("BACK", () -> game.returnTo(returnScreen));
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(960, 540);
        } else {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        }
    }

    private float nextVolume(float volume) {
        return volume <= 0f ? 1f : Math.max(0f, volume - 0.25f);
    }

    private String percent(float volume) {
        return Math.round(volume * 100f) + "%";
    }
}
