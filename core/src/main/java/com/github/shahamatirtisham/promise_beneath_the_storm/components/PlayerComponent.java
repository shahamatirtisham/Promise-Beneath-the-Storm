package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

public class PlayerComponent implements Component {
    public boolean dead;
    public boolean controlsLocked;
    /** Temporary development-only toggle controlled by F2 in GameScreen. */
    public boolean debugGodMode;
}
