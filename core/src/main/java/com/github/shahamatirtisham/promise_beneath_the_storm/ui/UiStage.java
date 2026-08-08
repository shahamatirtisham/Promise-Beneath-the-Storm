package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * A full-window stage that synchronizes its viewport before hit testing.
 *
 * Gameplay renders through a FitViewport, so relying on the last active OpenGL
 * viewport can offset Scene2D hover and click coordinates after scaling/resizing.
 */
public class UiStage extends Stage {
    public UiStage() {
        super(new ScreenViewport());
        synchronize();
    }

    public void synchronize() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (getViewport().getScreenWidth() != width
            || getViewport().getScreenHeight() != height) {
            getViewport().update(width, height, true);
        }
        getViewport().apply(true);
    }

    @Override public boolean mouseMoved(int screenX, int screenY) {
        synchronize();
        return super.mouseMoved(screenX, screenY);
    }

    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        synchronize();
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        synchronize();
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
        synchronize();
        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override public void draw() {
        synchronize();
        super.draw();
    }
}
