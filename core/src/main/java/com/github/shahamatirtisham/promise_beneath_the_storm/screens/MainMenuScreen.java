package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.MenuStyles;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.SettingsMenuBuilder;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.UiStage;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.ControlsMenuBuilder;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import java.util.EnumMap;

/** Main menu and its settings, credits, and exit modal panels. */
public class MainMenuScreen extends ScreenAdapter {
    private static final float MAIN_BUTTON_HEIGHT = 57.2f;
    private enum ModalView {
        NONE,
        CREDITS,
        SETTINGS,
        CONTROLS,
        EXIT
    }

    private final Main game;
    private final Stage stage = new UiStage();
    private final MenuStyles styles = new MenuStyles();
    private final Texture backgroundTexture;
    private final Table root = new Table();
    private final Array<TextButton> mainButtons = new Array<>();
    private TextButton continueButton;
    private Table modal;
    private ModalView modalView = ModalView.NONE;
    private final EnumMap<Action, TextButton> bindingButtons =
        new EnumMap<>(Action.class);
    private Action waitingForBinding;

    public MainMenuScreen(Main game) {
        this(game, false);
    }

    public MainMenuScreen(Main game, boolean openSettings) {
        this.game = game;
        stage.addCaptureListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (waitingForBinding == null) return false;
                if (keycode == Input.Keys.ESCAPE) {
                    waitingForBinding = null;
                } else {
                    applyBinding(keycode, false);
                }
                return true;
            }

            @Override public boolean touchDown(
                InputEvent event, float x, float y, int pointer, int button
            ) {
                if (waitingForBinding == null) return false;
                applyBinding(button, true);
                return true;
            }
        });
        backgroundTexture = new Texture(
            Gdx.files.internal("backgrounds/main-menu-bg.png")
        );
        backgroundTexture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );
        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill);
        background.setColor(0.72f, 0.74f, 0.8f, 1f);
        Table backgroundLayer = new Table();
        backgroundLayer.setFillParent(true);
        backgroundLayer.add(background).grow();
        stage.addActor(backgroundLayer);
        root.setFillParent(true);
        stage.addActor(root);
        buildMainMenu();
        if (openSettings) showSettings();
    }

    private void buildMainMenu() {
        root.clear();
        Label title = new Label("PROMISE BENEATH THE STORM", styles.title);
        title.setFontScale(2.1f);
        root.add(title).padBottom(55f);
        root.row();

        continueButton = menuButton("CONTINUE");
        mainButtons.add(continueButton);
        continueButton.setDisabled(!GamePreferences.hasCheckpoint());
        continueButton.addListener(change(() -> game.continueGame()));
        root.add(continueButton).width(280f).height(MAIN_BUTTON_HEIGHT).padBottom(12f); root.row();

        TextButton newGame = menuButton("NEW GAME");
        mainButtons.add(newGame);
        newGame.addListener(change(() -> game.startNewGame()));
        root.add(newGame).width(280f).height(MAIN_BUTTON_HEIGHT).padBottom(12f); root.row();

        TextButton settings = menuButton("SETTINGS");
        mainButtons.add(settings);
        settings.addListener(change(this::showSettings));
        root.add(settings).width(280f).height(MAIN_BUTTON_HEIGHT).padBottom(12f); root.row();

        TextButton credits = menuButton("CREDITS");
        mainButtons.add(credits);
        credits.addListener(change(game::showCredits));
        root.add(credits).width(280f).height(MAIN_BUTTON_HEIGHT).padBottom(12f); root.row();

        TextButton exit = menuButton("EXIT");
        mainButtons.add(exit);
        exit.addListener(change(this::showExit));
        root.add(exit).width(280f).height(MAIN_BUTTON_HEIGHT);
    }

    private TextButton menuButton(String text) { return new TextButton(text, styles.button); }

    private void showExit() {
        Table content = beginModal("Exit the game?");
        modalView = ModalView.EXIT;
        modal.getCell(content)
            .width(SettingsMenuBuilder.PANEL_WIDTH)
            .height(SettingsMenuBuilder.PANEL_HEIGHT);
        TextButton yes = new TextButton("YES", styles.button);
        yes.addListener(change(() -> Gdx.app.exit()));
        TextButton no = new TextButton("NO", styles.button);
        no.addListener(change(this::closeModal));
        content.add(yes).width(125f).height(48f).padRight(14f);
        content.add(no).width(125f).height(48f);
    }

    private void showCredits() {
        Table content = beginModal("CREDITS");
        modalView = ModalView.CREDITS;
        modal.getCell(content)
            .width(SettingsMenuBuilder.PANEL_WIDTH)
            .height(SettingsMenuBuilder.PANEL_HEIGHT);
        content.add(new Label("", styles.label)).height(170f).colspan(2);
        content.row();
        addCloseButton(content, 2);
    }

    private void showSettings() {
        Table content = beginModal("SETTINGS");
        modalView = ModalView.SETTINGS;
        modal.getCell(content).width(SettingsMenuBuilder.PANEL_WIDTH);
        SettingsMenuBuilder.populate(
            content,
            styles,
            this::showControls,
            this::closeModal
        );
        modal.getCell(content).height(SettingsMenuBuilder.PANEL_HEIGHT);
    }

    private void showControls() {
        closeModal();
        root.setTouchable(Touchable.disabled);
        setMainButtonsDisabled(true);
        modalView = ModalView.CONTROLS;
        modal = new Table();
        modal.setFillParent(true);
        stage.addActor(modal);
        Table panel = ControlsMenuBuilder.createPanel(styles);
        ControlsMenuBuilder.populate(
            panel,
            styles,
            bindingButtons,
            this::beginBindingCapture,
            () -> {
                GamePreferences.resetBindings();
                refreshBindingLabels();
            },
            this::showSettings
        );
        modal.add(ControlsMenuBuilder.scrollable(panel))
            .width(ControlsMenuBuilder.PANEL_WIDTH)
            .height(Value.percentHeight(ControlsMenuBuilder.PANEL_HEIGHT_RATIO, modal))
            .pad(12f);
    }

    private void beginBindingCapture(Action action) {
        waitingForBinding = action;
        bindingButtons.get(action).setText("PRESS INPUT...");
    }

    private void applyBinding(int code, boolean mouse) {
        Action action = waitingForBinding;
        waitingForBinding = null;
        GamePreferences.setBinding(action, code, mouse);
        refreshBindingLabels();
    }

    private void refreshBindingLabels() {
        for (Action action : Action.values()) {
            bindingButtons.get(action).setText(GamePreferences.bindingName(action));
        }
    }

    private Table beginModal(String heading) {
        closeModal();
        root.setTouchable(Touchable.disabled);
        setMainButtonsDisabled(true);
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

    private void closeModal() {
        if (modal != null) {
            modal.remove();
            modal = null;
        }
        root.setTouchable(Touchable.enabled);
        setMainButtonsDisabled(false);
        waitingForBinding = null;
        modalView = ModalView.NONE;
    }

    private void setMainButtonsDisabled(boolean modalOpen) {
        for (TextButton button : mainButtons) {
            button.setDisabled(modalOpen);
        }
        if (!modalOpen && continueButton != null) {
            continueButton.setDisabled(!GamePreferences.hasCheckpoint());
        }
    }

    private ChangeListener change(final Runnable runnable) {
        return new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { runnable.run(); } };
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (modalView == ModalView.CONTROLS) {
                showSettings();
            } else if (modalView != ModalView.NONE) {
                closeModal();
            } else {
                showExit();
            }
        }
        ScreenUtils.clear(0.015f, 0.02f, 0.04f, 1f);
        stage.act(Math.min(delta, 1f / 30f)); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        stage.dispose();
        styles.dispose();
        backgroundTexture.dispose();
    }
}
