package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class VictoryScreen extends BaseMenuScreen {
    public VictoryScreen(final Main game) {
        super(game, "IRHOS DEFEATED");
        addLabel("The promise beneath the storm has been fulfilled.");
        addButton("NEW RUN", game::startNewRun);
        addButton("MAIN MENU", game::showMainMenu);
    }
}
