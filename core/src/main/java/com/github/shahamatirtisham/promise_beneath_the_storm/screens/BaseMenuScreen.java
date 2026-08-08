package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.MenuStyles;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.UiStage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** Shared asset-backed menu layout for standalone game menus and dialogs. */
public abstract class BaseMenuScreen extends ScreenAdapter {
    private static final float PANEL_WIDTH = 640f;
        private static final float BUTTON_WIDTH = 251.875f;
    private static final float BUTTON_HEIGHT = 45f;

    protected final Main game;
    protected final Stage stage;
    protected final Table menu;
    protected final MenuStyles styles;
    private final Texture backgroundTexture;

    protected BaseMenuScreen(Main game, String title) {
        this.game = game;
        stage = new UiStage();
        styles = new MenuStyles();
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/main-menu-bg.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill);
        background.setColor(0.72f, 0.74f, 0.8f, 1f);
        Table backgroundLayer = new Table();
        backgroundLayer.setFillParent(true);
        backgroundLayer.add(background).grow();
        stage.addActor(backgroundLayer);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        menu = new Table();
        menu.setBackground(styles.panel);
        menu.pad(34f);
        root.add(menu).width(PANEL_WIDTH).height(420f).pad(20f);

        Label heading = new Label(title, styles.title);
        heading.setFontScale(1.5f);
        menu.add(heading).center().padBottom(24f);
        menu.row();
    }

    protected void addLabel(String text) {
        Label label = new Label(text, styles.label);
        label.setWrap(true);
        label.setAlignment(Align.center);
        menu.add(label).expandX().fillX().center().padBottom(16f);
        menu.row();
    }

    protected void addCompactLabel(String firstLine, String secondLine) {
        Table compact = new Table();
        Label first = new Label(firstLine, styles.label);
        Label second = new Label(secondLine, styles.label);
        first.setAlignment(Align.center);
        second.setAlignment(Align.center);
        compact.add(first).expandX().fillX();
        compact.row();
        compact.add(second).expandX().fillX();
        menu.add(compact).expandX().fillX().padBottom(16f);
        menu.row();
    }

    protected void addButton(String text, final Runnable action) {
        addButton(text, action, BUTTON_WIDTH);
    }

    protected void addButton(String text, final Runnable action, float width) {
        TextButton button = new TextButton(text, styles.button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                action.run();
            }
        });
        menu.add(button).width(width).height(BUTTON_HEIGHT).padBottom(10f);
        menu.row();
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        styles.dispose();
        backgroundTexture.dispose();
    }
}
