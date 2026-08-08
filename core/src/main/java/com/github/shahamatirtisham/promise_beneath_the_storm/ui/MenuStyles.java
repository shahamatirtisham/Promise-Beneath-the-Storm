package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Small programmatic UI skin so menus do not depend on external skin assets. */
public final class MenuStyles implements Disposable {
    public final BitmapFont font;
    public final Label.LabelStyle label;
    public final Label.LabelStyle title;
    public final TextButton.TextButtonStyle button;
    public final TextButton.TextButtonStyle redButton;
    public final TextButton.TextButtonStyle greenButton;
    public final Slider.SliderStyle slider;
    public final CheckBox.CheckBoxStyle checkBox;
    public final ImageButton.ImageButtonStyle audioToggle;
    public final TextureRegionDrawable panel;
    private final Array<Texture> textures = new Array<>();

    public MenuStyles() {
        font = GameFonts.create(18);
        label = new Label.LabelStyle(font, Color.WHITE);
        title = new Label.LabelStyle(font, new Color(0.92f, 0.78f, 0.42f, 1f));
        Texture buttonTexture = new Texture(
            Gdx.files.internal("ui/buttons/button-normal.png")
        );
        buttonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(buttonTexture);
        TextureRegionDrawable buttonArtwork = new TextureRegionDrawable(
            new TextureRegion(buttonTexture)
        );
        button = buttonStyle(
            buttonArtwork,
            Color.WHITE,
            new Color(1f, 0.9f, 0.62f, 1f),
            new Color(0.68f, 0.68f, 0.72f, 1f)
        );
        redButton = buttonStyle(
            buttonArtwork,
            new Color(0.82f, 0.28f, 0.25f, 1f),
            new Color(1f, 0.38f, 0.3f, 1f),
            new Color(0.55f, 0.12f, 0.1f, 1f)
        );
        greenButton = buttonStyle(
            buttonArtwork,
            new Color(0.3f, 0.82f, 0.42f, 1f),
            new Color(0.42f, 1f, 0.55f, 1f),
            new Color(0.16f, 0.55f, 0.25f, 1f)
        );
        Texture dialogTexture = new Texture(
            Gdx.files.internal("backgrounds/dialogue-bg.png")
        );
        dialogTexture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );
        textures.add(dialogTexture);
        panel = new TextureRegionDrawable(new TextureRegion(dialogTexture));
        panel.setMinWidth(0f);
        panel.setMinHeight(0f);
        slider = new Slider.SliderStyle(drawable(new Color(0.18f, 0.2f, 0.27f, 1f)), drawable(new Color(0.9f, 0.68f, 0.22f, 1f)));
        slider.knob.setMinWidth(18f); slider.knob.setMinHeight(28f);
        checkBox = new CheckBox.CheckBoxStyle(drawable(new Color(0.18f, 0.2f, 0.27f, 1f)), drawable(new Color(0.1f, 0.72f, 0.25f, 1f)), font, Color.WHITE);
        checkBox.checkboxOff.setMinWidth(24f); checkBox.checkboxOff.setMinHeight(24f);
        checkBox.checkboxOn.setMinWidth(24f); checkBox.checkboxOn.setMinHeight(24f);
        audioToggle = new ImageButton.ImageButtonStyle();
        audioToggle.up = button.up;
        audioToggle.over = button.over;
        audioToggle.down = button.down;
        audioToggle.checked = button.down;
        audioToggle.imageUp = audioIcon(false);
        audioToggle.imageChecked = audioIcon(true);
    }

    private TextButton.TextButtonStyle buttonStyle(
        TextureRegionDrawable artwork,
        Color normalTint,
        Color hoverTint,
        Color pressedTint
    ) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = artwork.tint(normalTint);
        style.over = artwork.tint(hoverTint);
        style.down = artwork.tint(pressedTint);
        style.disabled = artwork.tint(new Color(0.3f, 0.3f, 0.32f, 0.72f));
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = new Color(1f, 0.94f, 0.72f, 1f);
        style.downFontColor = Color.LIGHT_GRAY;
        style.disabledFontColor = new Color(0.42f, 0.42f, 0.45f, 1f);
        style.pressedOffsetY = -2f;
        return style;
    }

    private TextureRegionDrawable drawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color); pixmap.fill();
        Texture texture = new Texture(pixmap); pixmap.dispose(); textures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private TextureRegionDrawable audioIcon(boolean muted) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(4, 12, 7, 9);
        pixmap.fillTriangle(11, 12, 19, 7, 19, 26);
        if (muted) {
            pixmap.setColor(new Color(0.95f, 0.25f, 0.2f, 1f));
            pixmap.drawLine(22, 11, 29, 22);
            pixmap.drawLine(29, 11, 22, 22);
            pixmap.drawLine(23, 11, 30, 22);
            pixmap.drawLine(30, 11, 23, 22);
        } else {
            pixmap.drawLine(22, 12, 25, 16);
            pixmap.drawLine(25, 16, 22, 21);
            pixmap.drawLine(25, 9, 30, 16);
            pixmap.drawLine(30, 16, 25, 24);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        TextureRegionDrawable icon = new TextureRegionDrawable(new TextureRegion(texture));
        icon.setMinWidth(24f);
        icon.setMinHeight(24f);
        return icon;
    }

    @Override public void dispose() { font.dispose(); for (Texture texture : textures) texture.dispose(); }
}
