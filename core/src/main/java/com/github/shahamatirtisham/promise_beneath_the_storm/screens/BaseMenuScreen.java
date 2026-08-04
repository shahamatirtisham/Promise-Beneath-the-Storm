package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;

/** Shared programmatic menu styling until final UI artwork is supplied. */
public abstract class BaseMenuScreen extends ScreenAdapter {
    protected final Main game;
    protected final Stage stage;
    protected final Table menu;
    private final BitmapFont font;
    private final Texture buttonTexture;
    private final Texture pressedTexture;
    private final TextButton.TextButtonStyle buttonStyle;
    private final Label.LabelStyle labelStyle;

    protected BaseMenuScreen(Main game, String title) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        menu = new Table();
        menu.setFillParent(true);
        menu.center();
        stage.addActor(menu);

        font = new BitmapFont();
        font.getData().setScale(1.25f);
        buttonTexture = createTexture(new Color(0.16f, 0.12f, 0.23f, 1f));
        pressedTexture = createTexture(new Color(0.42f, 0.16f, 0.18f, 1f));
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        buttonStyle.down = new TextureRegionDrawable(new TextureRegion(pressedTexture));
        buttonStyle.over = buttonStyle.down;
        labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Label heading = new Label(title, labelStyle);
        heading.setFontScale(1.65f);
        menu.add(heading).padBottom(28f);
        menu.row();
    }

    protected void addLabel(String text) {
        menu.add(new Label(text, labelStyle)).padBottom(14f);
        menu.row();
    }

    protected void addButton(String text, final Runnable action) {
        TextButton button = new TextButton(text, buttonStyle);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                action.run();
            }
        });
        menu.add(button).width(280f).height(48f).padBottom(10f);
        menu.row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.025f, 0.02f, 0.045f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
        font.dispose();
        buttonTexture.dispose();
        pressedTexture.dispose();
    }

    private Texture createTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
