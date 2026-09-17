package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

public class CreditsScreen extends BaseMenuScreen {
    static final java.util.List<String> CREDIT_LINES = java.util.Collections.unmodifiableList(
        java.util.Arrays.asList(
            "Design: Irtisham, Abtahi, Arkam",
            "Programming: Irtisham",
            "Maps: Abtahi",
            "Sprites: Arkam",
            "Built with Java, LibGDX, Ashley ECS, Box2D and Tiled"
        )
    );

    public CreditsScreen(final Main game) {
        super(game, "CREDITS");
        for (String line : CREDIT_LINES) addLabel(line);
        addButton("BACK", game::showMainMenu);
    }
}
