package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.GameScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.MainMenuScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    public void startNewGame() {
        GamePreferences.clearCheckpoint();
        switchTo(new GameScreen());
    }

    public void continueGame() {
        if (GamePreferences.hasCheckpoint()) switchTo(new GameScreen(GamePreferences.loadCheckpoint()));
    }

    public void showMainMenu() {
        switchTo(new MainMenuScreen(this));
    }

    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) previous.dispose();
    }
}
