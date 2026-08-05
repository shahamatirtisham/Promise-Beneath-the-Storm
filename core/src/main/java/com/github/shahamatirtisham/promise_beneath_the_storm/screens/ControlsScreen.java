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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.EnumMap;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.MenuStyles;

/** Persistent keyboard and mouse control rebinding screen. */
public class ControlsScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage = new Stage(new ScreenViewport());
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
        Table root = new Table(); root.setFillParent(true); root.setBackground(styles.panel); root.pad(30f);
        stage.addActor(root);
        Label title = new Label("CONTROLS", styles.title); title.setFontScale(2f);
        root.add(title).colspan(2).padBottom(28f); root.row();
        root.add(new Label("ACTION", styles.label)).left().width(230f).padBottom(10f);
        root.add(new Label("BINDING", styles.label)).padBottom(10f); root.row();
        for (final Action action : Action.values()) {
            root.add(new Label(action.label, styles.label)).left().pad(6f);
            TextButton binding = new TextButton(GamePreferences.bindingName(action), styles.button);
            binding.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) { beginCapture(action); }
            });
            bindingButtons.put(action, binding);
            root.add(binding).width(210f).height(40f).pad(5f); root.row();
        }
        Label hint = new Label("Select a binding, then press a key or mouse button. Esc cancels.", styles.label);
        root.add(hint).colspan(2).padTop(16f).padBottom(18f); root.row();
        Table actions = new Table();
        TextButton defaults = new TextButton("RESTORE DEFAULTS", styles.button);
        defaults.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
            GamePreferences.resetBindings(); refreshLabels();
        }});
        TextButton back = new TextButton("BACK", styles.button);
        back.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
            game.showMainMenu();
        }});
        actions.add(defaults).width(210f).height(46f).padRight(12f);
        actions.add(back).width(160f).height(46f);
        root.add(actions).colspan(2);
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
    @Override public void render(float delta) { ScreenUtils.clear(0.015f, 0.02f, 0.04f, 1f); stage.act(delta); stage.draw(); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); styles.dispose(); }
}
