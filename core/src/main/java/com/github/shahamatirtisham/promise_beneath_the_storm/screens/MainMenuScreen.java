package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class MainMenuScreen extends BaseMenuScreen {
    public MainMenuScreen(final Main game) {
        super(game, "PROMISE BENEATH THE STORM");
        addLabel("A top-down action roguelike");
        addButton("NEW RUN", game::startNewRun);
        addButton("SETTINGS", () -> game.showSettings(this));
        addButton("CREDITS", game::showCredits);
        addButton("EXIT", () -> Gdx.app.exit());
    }
}
