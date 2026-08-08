package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.ControlsScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.CreditsScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.GameOverScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.GameScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.LevelUpgradeScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.MainMenuScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.PauseMenuScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.SettingsScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.screens.VictoryScreen;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private final Runnable toggleFullscreen;
    private GameScreen activeRun;
    public float musicVolume = 1f;
    public float soundVolume = 1f;

    public Main() {
        this(() -> {});
    }

    public Main(Runnable toggleFullscreen) {
        this.toggleFullscreen = toggleFullscreen;
    }

    @Override
    public void create() {
        showMainMenu();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen.run();
        }
        super.render();
    }

    /** Starts a clean run from Abtahi's main-menu New Game button. */
    public void startNewGame() {
        GamePreferences.clearCheckpoint();
        startNewRun();
    }

    public void startNewRun() {
        disposeCurrentMenu();
        disposeActiveRun();
        activeRun = new GameScreen(this);
        setScreen(activeRun);
    }

    /** Restores the persistent checkpoint exposed by Abtahi's Continue button. */
    public void continueGame() {
        if (!GamePreferences.hasCheckpoint()) {
            return;
        }
        disposeCurrentMenu();
        disposeActiveRun();
        activeRun = new GameScreen(this, GamePreferences.loadCheckpoint());
        setScreen(activeRun);
    }

    public void showMainMenu() {
        disposeCurrentMenu();
        disposeActiveRun();
        setScreen(new MainMenuScreen(this));
    }

    public void showMainMenuSettings() {
        disposeCurrentMenu();
        disposeActiveRun();
        setScreen(new MainMenuScreen(this, true));
    }

    public void showControls() {
        disposeCurrentMenu();
        disposeActiveRun();
        setScreen(new ControlsScreen(this));
    }

    /** Retained for the standalone pause screen; gameplay now uses Abtahi's HUD pause overlay. */
    public void showPauseMenu(GameScreen run) {
        activeRun = run;
        setScreen(new PauseMenuScreen(this));
    }

    public void resumeRun() {
        disposeCurrentMenu();
        setScreen(activeRun);
    }

    public void restartFromCheckpoint() {
        disposeCurrentMenu();
        activeRun.restoreLatestCheckpoint();
        setScreen(activeRun);
    }

    public void showGameOver(GameScreen run) {
        activeRun = run;
        setScreen(new GameOverScreen(this));
    }

    public void showVictory(GameScreen run) {
        activeRun = run;
        run.showVictoryOverlay(this::startNewRun);
    }

    public void showLevelUpgrade(GameScreen run) {
        activeRun = run;
        run.showLevelUpgradeOverlay(type -> acceptLevelUpgrade(run, type));
    }

    public void acceptLevelUpgrade(GameScreen run, RelicType type) {
        disposeCurrentMenu();
        run.acceptLevelUpgrade(type);
        setScreen(run);
    }

    public void showSettings(Screen returnScreen) {
        Screen current = getScreen();
        setScreen(new SettingsScreen(this, returnScreen));
        if (current != null && current != returnScreen && current != activeRun) {
            current.dispose();
        }
    }

    public void showCredits() {
        disposeCurrentMenu();
        setScreen(new CreditsScreen(this));
    }

    public void returnTo(Screen target) {
        Screen current = getScreen();
        setScreen(target);
        if (current != target && current != activeRun) {
            current.dispose();
        }
    }

    private void disposeCurrentMenu() {
        Screen current = getScreen();
        if (current != null && current != activeRun) {
            current.dispose();
        }
    }

    private void disposeActiveRun() {
        if (activeRun != null) {
            activeRun.dispose();
            activeRun = null;
        }
    }

    @Override
    public void dispose() {
        disposeCurrentMenu();
        disposeActiveRun();
    }
}
