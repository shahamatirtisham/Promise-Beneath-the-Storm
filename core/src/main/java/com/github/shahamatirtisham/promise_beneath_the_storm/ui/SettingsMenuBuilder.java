package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** Builds the one canonical settings layout used by every menu. */
public final class SettingsMenuBuilder {
    private SettingsMenuBuilder() {}

    public static void populate(
        Table panel,
        MenuStyles styles,
        Runnable controlsAction,
        Runnable backAction
    ) {
        Slider music = new Slider(0f, 10f, 1f, false, styles.slider);
        music.setValue(GamePreferences.getMusicLevel());
        Label musicValue = new Label(Integer.toString((int) music.getValue()), styles.label);
        CheckBox musicMute = new CheckBox("  Mute music", styles.checkBox);
        musicMute.setChecked(GamePreferences.isMusicMuted());

        Slider sound = new Slider(0f, 10f, 1f, false, styles.slider);
        sound.setValue(GamePreferences.getSoundLevel());
        Label soundValue = new Label(Integer.toString((int) sound.getValue()), styles.label);
        CheckBox soundMute = new CheckBox("  Mute sounds", styles.checkBox);
        soundMute.setChecked(GamePreferences.isSoundMuted());

        ChangeListener save = new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                musicValue.setText(Integer.toString((int) music.getValue()));
                soundValue.setText(Integer.toString((int) sound.getValue()));
                GamePreferences.saveAudio(
                    (int) music.getValue(), musicMute.isChecked(),
                    (int) sound.getValue(), soundMute.isChecked()
                );
            }
        };
        music.addListener(save); musicMute.addListener(save);
        sound.addListener(save); soundMute.addListener(save);

        panel.add(new Label("Music level", styles.label)).left().padBottom(8f);
        panel.add(musicValue).width(30f).padBottom(8f); panel.row();
        panel.add(music).width(300f).height(30f).padBottom(10f);
        panel.add().padBottom(10f); panel.row();
        panel.add(musicMute).left().colspan(2).padBottom(22f); panel.row();
        panel.add(new Label("Sound level", styles.label)).left().padBottom(8f);
        panel.add(soundValue).width(30f).padBottom(8f); panel.row();
        panel.add(sound).width(300f).height(30f).padBottom(10f);
        panel.add().padBottom(10f); panel.row();
        panel.add(soundMute).left().colspan(2).padBottom(24f); panel.row();

        TextButton controls = new TextButton("CONTROLS", styles.button);
        controls.addListener(change(controlsAction));
        panel.add(controls).width(220f).height(46f).colspan(2).padBottom(14f); panel.row();
        TextButton back = new TextButton("BACK", styles.button);
        back.addListener(change(backAction));
        panel.add(back).width(180f).height(44f).colspan(2);
    }

    private static ChangeListener change(Runnable action) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { action.run(); }
        };
    }
}
