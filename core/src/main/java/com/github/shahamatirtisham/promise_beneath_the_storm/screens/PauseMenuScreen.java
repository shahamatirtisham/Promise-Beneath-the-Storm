package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class PauseMenuScreen extends BaseMenuScreen {
    public PauseMenuScreen(final Main game) {
        super(game, "PAUSED");
        addButton("RESUME", game::resumeRun);
        addButton("SETTINGS", () -> game.showSettings(this));
        addButton("RESTART RUN", game::startNewRun);
        addButton("MAIN MENU", game::showMainMenu);
    }
}
