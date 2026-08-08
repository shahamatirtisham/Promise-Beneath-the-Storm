package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class CreditsScreen extends BaseMenuScreen {
    public CreditsScreen(final Main game) {
        super(game, "CREDITS");
        addLabel("Game Design & Programming: Irtisham oh yeah");
        addLabel("Sprites & Visuals: Arkam and Abtahi");
        addLabel("Built with Java, LibGDX, Ashley ECS, Box2D and Tiled");
        addButton("BACK", game::showMainMenu);
    }
}
