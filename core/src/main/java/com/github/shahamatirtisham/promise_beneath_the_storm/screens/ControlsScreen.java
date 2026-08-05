package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.EnumMap;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.MenuStyles;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.UiStage;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.ControlsMenuBuilder;

/** Persistent keyboard and mouse control rebinding screen. */
public class ControlsScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage = new UiStage();
    private final MenuStyles styles = new MenuStyles();
    private final EnumMap<Action, TextButton> bindingButtons = new EnumMap<>(Action.class);
    private Action waitingFor;
    private final InputAdapter capture = new InputAdapter() {
        @Override public boolean keyDown(int keycode) {
            if (waitingFor == null) return false;
            if (keycode == Input.Keys.ESCAPE) { cancelCapture(); return true; }
            applyBinding(keycode, false); return true;
        }
        @Override public boolean touchDown(int x, int y, int pointer, int button) {
            if (waitingFor == null) return false;
            applyBinding(button, true); return true;
        }
    };

    public ControlsScreen(Main game) {
        this.game = game;
        Table root = new Table(); root.setFillParent(true);
        stage.addActor(root);
        Table panel = ControlsMenuBuilder.createPanel(styles);
        ControlsMenuBuilder.populate(
            panel,
            styles,
            bindingButtons,
            this::beginCapture,
            () -> {
                GamePreferences.resetBindings();
                refreshLabels();
            },
            game::showMainMenuSettings
        );
        root.add(ControlsMenuBuilder.scrollable(panel))
            .width(ControlsMenuBuilder.PANEL_WIDTH)
            .height(Value.percentHeight(ControlsMenuBuilder.PANEL_HEIGHT_RATIO, root))
            .pad(12f);
    }

    private void beginCapture(Action action) {
        cancelCapture(); waitingFor = action; bindingButtons.get(action).setText("PRESS INPUT...");
    }
    private void cancelCapture() {
        if (waitingFor != null) bindingButtons.get(waitingFor).setText(GamePreferences.bindingName(waitingFor));
        waitingFor = null;
    }
    private void applyBinding(int code, boolean mouse) {
        Action action = waitingFor; waitingFor = null;
        GamePreferences.setBinding(action, code, mouse); refreshLabels();
    }
    private void refreshLabels() {
        for (Action action : Action.values()) bindingButtons.get(action).setText(GamePreferences.bindingName(action));
    }
    @Override public void show() { Gdx.input.setInputProcessor(new InputMultiplexer(capture, stage)); }
    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenuSettings();
            return;
        }
        ScreenUtils.clear(0.015f, 0.02f, 0.04f, 1f);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); styles.dispose(); }
}
