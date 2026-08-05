package com.github.shahamatirtisham.promise_beneath_the_storm.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/** Persistent audio, controls, and checkpoint storage. */
public final class GamePreferences {
    private static final String NAME = "promise-beneath-the-storm";

    public enum Action {
        MOVE_UP("Move up", Input.Keys.W, false),
        MOVE_DOWN("Move down", Input.Keys.S, false),
        MOVE_LEFT("Move left", Input.Keys.A, false),
        MOVE_RIGHT("Move right", Input.Keys.D, false),
        ATTACK("Attack", Input.Buttons.LEFT, true),
        BLOCK("Block / parry", Input.Buttons.RIGHT, true),
        DASH("Dash", Input.Keys.SPACE, false),
        INTERACT("Interact", Input.Keys.E, false);

        public final String label;
        public final int defaultCode;
        public final boolean defaultMouse;

        Action(String label, int defaultCode, boolean defaultMouse) {
            this.label = label;
            this.defaultCode = defaultCode;
            this.defaultMouse = defaultMouse;
        }
    }

    private GamePreferences() {}

    private static Preferences prefs() {
        return Gdx.app.getPreferences(NAME);
    }

    public static int getMusicLevel() { return prefs().getInteger("musicLevel", 10); }
    public static int getSoundLevel() { return prefs().getInteger("soundLevel", 10); }
    public static boolean isMusicMuted() { return prefs().getBoolean("musicMuted", false); }
    public static boolean isSoundMuted() { return prefs().getBoolean("soundMuted", false); }

    public static void saveAudio(int music, boolean musicMuted, int sound, boolean soundMuted) {
        prefs().putInteger("musicLevel", clampLevel(music));
        prefs().putBoolean("musicMuted", musicMuted);
        prefs().putInteger("soundLevel", clampLevel(sound));
        prefs().putBoolean("soundMuted", soundMuted);
        prefs().flush();
    }

    private static int clampLevel(int value) { return Math.max(0, Math.min(10, value)); }

    public static int getCode(Action action) {
        return prefs().getInteger("control." + action.name() + ".code", action.defaultCode);
    }

    public static boolean isMouse(Action action) {
        return prefs().getBoolean("control." + action.name() + ".mouse", action.defaultMouse);
    }

    public static void setBinding(Action action, int code, boolean mouse) {
        prefs().putInteger("control." + action.name() + ".code", code);
        prefs().putBoolean("control." + action.name() + ".mouse", mouse);
        prefs().flush();
    }

    public static void resetBindings() {
        for (Action action : Action.values()) {
            prefs().remove("control." + action.name() + ".code");
            prefs().remove("control." + action.name() + ".mouse");
        }
        prefs().flush();
    }

    public static boolean isPressed(Action action) {
        int code = getCode(action);
        return isMouse(action) ? Gdx.input.isButtonPressed(code) : Gdx.input.isKeyPressed(code);
    }

    public static boolean isJustPressed(Action action) {
        int code = getCode(action);
        return isMouse(action) ? Gdx.input.isButtonJustPressed(code) : Gdx.input.isKeyJustPressed(code);
    }

    public static String bindingName(Action action) {
        int code = getCode(action);
        if (!isMouse(action)) {
            return Input.Keys.toString(code);
        }
        if (code == Input.Buttons.LEFT) return "Left Mouse";
        if (code == Input.Buttons.RIGHT) return "Right Mouse";
        if (code == Input.Buttons.MIDDLE) return "Middle Mouse";
        return "Mouse " + (code + 1);
    }

    public static boolean hasCheckpoint() { return prefs().getBoolean("checkpoint.exists", false); }

    public static void saveCheckpoint(RunCheckpoint checkpoint) {
        Preferences p = prefs();
        p.putBoolean("checkpoint.exists", true);
        p.putInteger("checkpoint.restartLevel", checkpoint.restartLevel);
        p.putBoolean("checkpoint.boss", checkpoint.bossCheckpoint);
        p.putFloat("checkpoint.maximumHealth", checkpoint.maximumHealth);
        p.putInteger("checkpoint.coins", checkpoint.devilCoins);
        p.putInteger("checkpoint.defeated", checkpoint.enemiesDefeated);
        p.putInteger("checkpoint.ironHeart", checkpoint.ironHeart);
        p.putInteger("checkpoint.stormEdge", checkpoint.stormEdge);
        p.putInteger("checkpoint.windstep", checkpoint.windstepSigil);
        p.putFloat("checkpoint.attackDamage", checkpoint.attackDamage);
        p.putFloat("checkpoint.dashCooldown", checkpoint.dashCooldown);
        p.flush();
    }

    public static RunCheckpoint loadCheckpoint() {
        if (!hasCheckpoint()) return null;
        Preferences p = prefs();
        RunCheckpoint checkpoint = new RunCheckpoint();
        checkpoint.capture(
            p.getInteger("checkpoint.restartLevel", 4),
            p.getBoolean("checkpoint.boss", false),
            p.getFloat("checkpoint.maximumHealth", 100f),
            p.getInteger("checkpoint.coins", 0),
            p.getInteger("checkpoint.defeated", 0),
            p.getInteger("checkpoint.ironHeart", 0),
            p.getInteger("checkpoint.stormEdge", 0),
            p.getInteger("checkpoint.windstep", 0),
            p.getFloat("checkpoint.attackDamage", 15f),
            p.getFloat("checkpoint.dashCooldown", 1.2f)
        );
        return checkpoint;
    }

    public static void clearCheckpoint() {
        Preferences p = prefs();
        String[] keys = {"exists", "restartLevel", "boss", "maximumHealth", "coins",
            "defeated", "ironHeart", "stormEdge", "windstep", "attackDamage", "dashCooldown"};
        for (String key : keys) p.remove("checkpoint." + key);
        p.flush();
    }
}
