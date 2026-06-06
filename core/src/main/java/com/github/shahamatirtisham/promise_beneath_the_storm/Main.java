package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.gdx.Game;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
