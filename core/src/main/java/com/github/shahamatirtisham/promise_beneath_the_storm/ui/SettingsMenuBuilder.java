package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** Builds the one canonical settings layout used by every menu. */
public final class SettingsMenuBuilder {
    public static final float PANEL_WIDTH = 630f;
    public static final float PANEL_HEIGHT = 440f;
    private static final float BUTTON_WIDTH = 220f;
    private static final float BUTTON_HEIGHT = 46f;
    private static final float SLIDER_WIDTH = 260f;

    private SettingsMenuBuilder() {}

    public static void populate(
        Table panel,
        MenuStyles styles,
        Runnable controlsAction,
        Runnable backAction
    ) {
        int savedMusicLevel = GamePreferences.getMusicLevel();
        Slider music = new Slider(0f, 10f, 1f, false, styles.slider);
        music.setValue(GamePreferences.isMusicMuted() ? 0f : savedMusicLevel);
        ImageButton musicMute = new ImageButton(styles.audioToggle);
        musicMute.setChecked(music.getValue() == 0f);
        final int[] lastMusicLevel = {Math.max(1, savedMusicLevel)};

        int savedSoundLevel = GamePreferences.getSoundLevel();
        Slider sound = new Slider(0f, 10f, 1f, false, styles.slider);
        sound.setValue(GamePreferences.isSoundMuted() ? 0f : savedSoundLevel);
        ImageButton soundMute = new ImageButton(styles.audioToggle);
        soundMute.setChecked(sound.getValue() == 0f);
        final int[] lastSoundLevel = {Math.max(1, savedSoundLevel)};

        ChangeListener musicLevelChanged = new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (music.getValue() == 0f) {
                    musicMute.setChecked(true);
                } else {
                    lastMusicLevel[0] = (int) music.getValue();
                    musicMute.setChecked(false);
                }
                saveAudio(music, musicMute, sound, soundMute);
            }
        };
        ChangeListener musicMuteChanged = new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (musicMute.isChecked()) {
                    if (music.getValue() > 0f) lastMusicLevel[0] = (int) music.getValue();
                    music.setValue(0f);
                } else if (music.getValue() == 0f) {
                    music.setValue(lastMusicLevel[0]);
                }
                saveAudio(music, musicMute, sound, soundMute);
            }
        };
        ChangeListener soundLevelChanged = new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (sound.getValue() == 0f) {
                    soundMute.setChecked(true);
                } else {
                    lastSoundLevel[0] = (int) sound.getValue();
                    soundMute.setChecked(false);
                }
                saveAudio(music, musicMute, sound, soundMute);
            }
        };
        ChangeListener soundMuteChanged = new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (soundMute.isChecked()) {
                    if (sound.getValue() > 0f) lastSoundLevel[0] = (int) sound.getValue();
                    sound.setValue(0f);
                } else if (sound.getValue() == 0f) {
                    sound.setValue(lastSoundLevel[0]);
                }
                saveAudio(music, musicMute, sound, soundMute);
            }
        };
        music.addListener(musicLevelChanged);
        musicMute.addListener(musicMuteChanged);
        sound.addListener(soundLevelChanged);
        soundMute.addListener(soundMuteChanged);

        Table audioRows = new Table();
        audioRows.add(new Label("Music", styles.label)).left().width(90f).padRight(14f).padBottom(20f);
        audioRows.add(music).width(SLIDER_WIDTH).height(30f).padRight(18f).padBottom(20f);
        audioRows.add(musicMute).width(52f).height(46f).padBottom(20f);
        audioRows.row();
        audioRows.add(new Label("Sound", styles.label)).left().width(90f).padRight(14f);
        audioRows.add(sound).width(SLIDER_WIDTH).height(30f).padRight(18f);
        audioRows.add(soundMute).width(52f).height(46f);
        panel.add(audioRows).colspan(2).padBottom(28f);
        panel.row();

        TextButton controls = new TextButton("CONTROLS", styles.button);
        controls.addListener(change(controlsAction));
        panel.add(controls)
            .width(BUTTON_WIDTH)
            .height(BUTTON_HEIGHT)
            .colspan(2)
            .padBottom(14f);
        panel.row();
        TextButton back = new TextButton("BACK", styles.button);
        back.addListener(change(backAction));
        panel.add(back)
            .width(BUTTON_WIDTH)
            .height(BUTTON_HEIGHT)
            .colspan(2);
    }

    private static void saveAudio(
        Slider music,
        ImageButton musicMute,
        Slider sound,
        ImageButton soundMute
    ) {
        GamePreferences.saveAudio(
            (int) music.getValue(), musicMute.isChecked(),
            (int) sound.getValue(), soundMute.isChecked()
        );
    }

    private static ChangeListener change(Runnable action) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { action.run(); }
        };
    }
}
