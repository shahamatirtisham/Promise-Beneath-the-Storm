package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class GameOverScreen extends BaseMenuScreen {
    public GameOverScreen(final Main game) {
        super(game, "THE STORM CLAIMED YOU");
        addLabel("Restore your latest milestone or begin again.");
        addButton("RESTORE CHECKPOINT", game::restartFromCheckpoint);
        addButton("NEW RUN", game::startNewRun);
        addButton("MAIN MENU", game::showMainMenu);
    }
}
