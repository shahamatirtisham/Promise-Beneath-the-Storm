package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Small programmatic UI skin so menus do not depend on external skin assets. */
public final class MenuStyles implements Disposable {
    public final BitmapFont font = new BitmapFont();
    public final Label.LabelStyle label;
    public final Label.LabelStyle title;
    public final TextButton.TextButtonStyle button;
    public final TextButton.TextButtonStyle redButton;
    public final TextButton.TextButtonStyle greenButton;
    public final Slider.SliderStyle slider;
    public final CheckBox.CheckBoxStyle checkBox;
    public final TextureRegionDrawable panel;
    private final Array<Texture> textures = new Array<>();

    public MenuStyles() {
        label = new Label.LabelStyle(font, Color.WHITE);
        title = new Label.LabelStyle(font, new Color(0.92f, 0.78f, 0.42f, 1f));
        button = buttonStyle(new Color(0.16f, 0.19f, 0.26f, 1f), new Color(0.26f, 0.32f, 0.43f, 1f));
        redButton = buttonStyle(new Color(0.65f, 0.09f, 0.08f, 1f), new Color(0.85f, 0.16f, 0.12f, 1f));
        greenButton = buttonStyle(new Color(0.08f, 0.5f, 0.18f, 1f), new Color(0.12f, 0.72f, 0.25f, 1f));
        panel = drawable(new Color(0.035f, 0.045f, 0.075f, 0.98f));
        slider = new Slider.SliderStyle(drawable(new Color(0.18f, 0.2f, 0.27f, 1f)), drawable(new Color(0.9f, 0.68f, 0.22f, 1f)));
        slider.knob.setMinWidth(18f); slider.knob.setMinHeight(28f);
        checkBox = new CheckBox.CheckBoxStyle(drawable(new Color(0.18f, 0.2f, 0.27f, 1f)), drawable(new Color(0.1f, 0.72f, 0.25f, 1f)), font, Color.WHITE);
        checkBox.checkboxOff.setMinWidth(24f); checkBox.checkboxOff.setMinHeight(24f);
        checkBox.checkboxOn.setMinWidth(24f); checkBox.checkboxOn.setMinHeight(24f);
    }

    private TextButton.TextButtonStyle buttonStyle(Color normal, Color over) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = drawable(normal); style.over = drawable(over); style.down = drawable(over.cpy().mul(0.75f));
        style.disabled = drawable(new Color(0.09f, 0.1f, 0.13f, 1f));
        style.font = font; style.fontColor = Color.WHITE; style.disabledFontColor = Color.DARK_GRAY;
        return style;
    }

    private TextureRegionDrawable drawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color); pixmap.fill();
        Texture texture = new Texture(pixmap); pixmap.dispose(); textures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override public void dispose() { font.dispose(); for (Texture texture : textures) texture.dispose(); }
}
