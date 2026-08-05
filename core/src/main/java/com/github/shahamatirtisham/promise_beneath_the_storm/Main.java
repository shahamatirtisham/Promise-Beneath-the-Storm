package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.GameScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.MainMenuScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.ControlsScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private final Runnable toggleFullscreen;

    public Main() {
        this(() -> {});
    }

    public Main(Runnable toggleFullscreen) {
        this.toggleFullscreen = toggleFullscreen;
    }

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen.run();
        }
        super.render();
    }

    public void startNewGame() {
        GamePreferences.clearCheckpoint();
        switchTo(new GameScreen(this));
    }

    public void continueGame() {
        if (GamePreferences.hasCheckpoint()) {
            switchTo(new GameScreen(this, GamePreferences.loadCheckpoint()));
        }
    }

    public void showMainMenu() {
        switchTo(new MainMenuScreen(this));
    }

    public void showMainMenuSettings() {
        switchTo(new MainMenuScreen(this, true));
    }

    public void showControls() {
        switchTo(new ControlsScreen(this));
    }

    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) previous.dispose();
    }
}
