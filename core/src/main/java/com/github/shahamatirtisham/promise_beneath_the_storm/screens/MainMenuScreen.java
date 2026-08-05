package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.MenuStyles;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.SettingsMenuBuilder;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.UiStage;

/** Main menu and its settings, credits, and exit modal panels. */
public class MainMenuScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage = new UiStage();
    private final MenuStyles styles = new MenuStyles();
    private final Table root = new Table();
    private Table modal;

    public MainMenuScreen(Main game) {
        this.game = game;
        root.setFillParent(true);
        stage.addActor(root);
        buildMainMenu();
    }

    private void buildMainMenu() {
        root.clear();
        root.setBackground(styles.panel);
        Label title = new Label("PROMISE BENEATH THE STORM", styles.title);
        title.setFontScale(2.1f);
        root.add(title).padBottom(55f);
        root.row();

        TextButton continueButton = menuButton("CONTINUE");
        continueButton.setDisabled(!GamePreferences.hasCheckpoint());
        continueButton.addListener(change(() -> game.continueGame()));
        root.add(continueButton).width(280f).height(52f).padBottom(12f); root.row();

        TextButton newGame = menuButton("NEW GAME");
        newGame.addListener(change(() -> game.startNewGame()));
        root.add(newGame).width(280f).height(52f).padBottom(12f); root.row();

        TextButton settings = menuButton("SETTINGS");
        settings.addListener(change(this::showSettings));
        root.add(settings).width(280f).height(52f).padBottom(12f); root.row();

        TextButton credits = menuButton("CREDITS");
        credits.addListener(change(this::showCredits));
        root.add(credits).width(280f).height(52f).padBottom(12f); root.row();

        TextButton exit = menuButton("EXIT");
        exit.addListener(change(this::showExit));
        root.add(exit).width(280f).height(52f);
    }

    private TextButton menuButton(String text) { return new TextButton(text, styles.button); }

    private void showExit() {
        Table content = beginModal("Exit the game?");
        TextButton yes = new TextButton("YES", styles.redButton);
        yes.addListener(change(() -> Gdx.app.exit()));
        TextButton no = new TextButton("NO", styles.greenButton);
        no.addListener(change(this::closeModal));
        content.add(yes).width(125f).height(48f).padRight(14f);
        content.add(no).width(125f).height(48f);
    }

    private void showCredits() {
        Table content = beginModal("CREDITS");
        content.add(new Label("", styles.label)).height(170f).colspan(2);
        content.row();
        addCloseButton(content, 2);
    }

    private void showSettings() {
        Table content = beginModal("SETTINGS");
        SettingsMenuBuilder.populate(
            content,
            styles,
            () -> game.setScreen(new ControlsScreen(game)),
            this::closeModal
        );
    }

    private Table beginModal(String heading) {
        closeModal();
        modal = new Table();
        modal.setFillParent(true);
        modal.setColor(Color.WHITE);
        stage.addActor(modal);
        Table content = new Table();
        content.setBackground(styles.panel);
        content.pad(30f);
        modal.add(content).minWidth(420f);
        Label title = new Label(heading, styles.title);
        title.setFontScale(1.5f);
        content.add(title).colspan(2).padBottom(28f);
        content.row();
        return content;
    }

    private void addCloseButton(Table content, int colspan) {
        TextButton close = menuButton("BACK");
        close.addListener(change(this::closeModal));
        content.add(close).width(180f).height(44f).colspan(colspan);
    }

    private void closeModal() { if (modal != null) { modal.remove(); modal = null; } }

    private ChangeListener change(final Runnable runnable) {
        return new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { runnable.run(); } };
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        ScreenUtils.clear(0.015f, 0.02f, 0.04f, 1f);
        stage.act(Math.min(delta, 1f / 30f)); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); styles.dispose(); }
}
