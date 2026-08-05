package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import java.util.EnumMap;

/** Fixed-screen gameplay information that does not reveal dungeon navigation. */
public class GameHud implements Disposable {
    private final Stage stage;
    private final BitmapFont font;
    private final Texture panelTexture;
    private final Texture healthBackgroundTexture;
    private final Texture healthFillTexture;
    private final Label healthLabel;
    private final Label coinsLabel;
    private final Label levelLabel;
    private final Label relicsLabel;
    private final Label statusLabel;
    private final Label dashLabel;
    private final Label comboLabel;
    private final Label defenseLabel;
    private final ProgressBar healthBar;
    private final Label bossLabel;
    private final ProgressBar bossHealthBar;
    private final Table bossPanel;
    private final MenuStyles menuStyles;
    private final Runnable restartAction;
    private final Runnable mainMenuAction;
    private Table pauseOverlay;
    private boolean paused;
    private boolean gameOver;
    private final EnumMap<Action, TextButton> pauseBindingButtons =
        new EnumMap<>(Action.class);
    private Action waitingForBinding;

    public GameHud(Runnable restartAction, Runnable mainMenuAction) {
        this.restartAction = restartAction;
        this.mainMenuAction = mainMenuAction;
        stage = new UiStage();
        menuStyles = new MenuStyles();
        stage.addCaptureListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (waitingForBinding == null) return false;
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    cancelBindingCapture();
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
        stage.getViewport().update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );
        font = new BitmapFont();
        font.getData().setScale(1.05f);

        panelTexture = createTexture(new Color(0.03f, 0.04f, 0.07f, 0.88f));
        healthBackgroundTexture = createTexture(new Color(0.22f, 0.04f, 0.05f, 1f));
        healthFillTexture = createTexture(new Color(0.12f, 0.82f, 0.25f, 1f));

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        healthLabel = new Label("", labelStyle);
        coinsLabel = new Label("", labelStyle);
        levelLabel = new Label("", labelStyle);
        relicsLabel = new Label("", labelStyle);
        statusLabel = new Label("", labelStyle);
        dashLabel = new Label("", labelStyle);
        comboLabel = new Label("", labelStyle);
        defenseLabel = new Label("", labelStyle);
        bossLabel = new Label("", labelStyle);

        ProgressBar.ProgressBarStyle healthStyle = new ProgressBar.ProgressBarStyle();
        healthStyle.background = new TextureRegionDrawable(
            new TextureRegion(healthBackgroundTexture)
        );
        healthStyle.knobBefore = new TextureRegionDrawable(
            new TextureRegion(healthFillTexture)
        );
        healthBar = new ProgressBar(0f, 1f, 0.01f, false, healthStyle);
        bossHealthBar = new ProgressBar(0f, 1f, 0.01f, false, healthStyle);

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(14f);
        stage.addActor(root);

        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panel.pad(10f);
        panel.defaults().left().padBottom(5f);
        root.add(panel).width(310f);

        panel.add(levelLabel).colspan(2).left();
        panel.row();
        panel.add(healthLabel).width(105f);
        panel.add(healthBar).width(175f).height(14f);
        panel.row();
        panel.add(coinsLabel).colspan(2);
        panel.row();
        panel.add(relicsLabel).colspan(2);
        panel.row();
        panel.add(statusLabel).colspan(2);
        panel.row();
        panel.add(dashLabel).colspan(2);
        panel.row();
        panel.add(comboLabel).colspan(2);
        panel.row();
        panel.add(defenseLabel).colspan(2);

        Table bossRoot = new Table();
        bossRoot.setFillParent(true);
        bossRoot.top();
        bossRoot.padTop(14f);
        stage.addActor(bossRoot);

        bossPanel = new Table();
        bossPanel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        bossPanel.pad(8f);
        bossPanel.add(bossLabel).center();
        bossPanel.row();
        bossPanel.add(bossHealthBar).width(360f).height(16f).padTop(5f);
        bossPanel.setVisible(false);
        bossRoot.add(bossPanel);

        Table pauseRoot = new Table();
        pauseRoot.setFillParent(true);
        pauseRoot.top().right().pad(14f);
        TextButton pauseButton = new TextButton("PAUSE", menuStyles.button);
        pauseButton.addListener(change(this::showPauseMenu));
        pauseRoot.add(pauseButton).width(105f).height(42f);
        stage.addActor(pauseRoot);
    }

    public Stage getStage() { return stage; }
    public boolean isPaused() { return paused || gameOver; }

    public void setGameOver(boolean dead) {
        if (!dead || gameOver) return;
        gameOver = true;
        paused = false;
        showGameOverMenu();
    }

    private void showGameOverMenu() {
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("GAME OVER");
        pauseOverlay.add(panel).width(440f);

        TextButton restart = new TextButton(
            "RESTART FROM CHECKPOINT", menuStyles.greenButton
        );
        restart.addListener(change(restartAction));
        panel.add(restart).width(320f).height(52f).padBottom(14f);
        panel.row();

        TextButton mainMenu = new TextButton("MAIN MENU", menuStyles.button);
        mainMenu.addListener(change(mainMenuAction));
        panel.add(mainMenu).width(320f).height(52f);
    }

    private void showPauseMenu() {
        if (paused) return;
        paused = true;
        showPauseButtons();
    }

    private void showPauseButtons() {
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("GAME PAUSED");
        pauseOverlay.add(panel).width(430f);

        TextButton resume = new TextButton("RESUME", menuStyles.greenButton);
        resume.addListener(change(this::closePauseMenu));
        panel.add(resume).width(310f).height(50f).padBottom(14f); panel.row();

        TextButton restart = new TextButton("RESTART FROM CHECKPOINT", menuStyles.button);
        restart.addListener(change(restartAction));
        panel.add(restart).width(310f).height(50f).padBottom(14f); panel.row();

        TextButton mainMenu = new TextButton("MAIN MENU", menuStyles.button);
        mainMenu.addListener(change(mainMenuAction));
        panel.add(mainMenu).width(310f).height(50f).padBottom(14f); panel.row();

        TextButton settings = new TextButton("SETTINGS", menuStyles.button);
        settings.addListener(change(this::showPauseSettings));
        panel.add(settings).width(310f).height(50f);
    }

    private void showPauseSettings() {
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("SETTINGS");
        pauseOverlay.add(panel).minWidth(420f);
        SettingsMenuBuilder.populate(
            panel, menuStyles, this::showPauseControls, this::showPauseButtons
        );
    }

    private void showPauseControls() {
        waitingForBinding = null;
        pauseBindingButtons.clear();
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("CONTROLS");
        pauseOverlay.add(panel).width(520f);
        panel.add(new Label("ACTION", menuStyles.label)).left().width(220f);
        panel.add(new Label("BINDING", menuStyles.label)); panel.row();
        for (Action action : Action.values()) {
            panel.add(new Label(action.label, menuStyles.label)).left().pad(2f);
            TextButton binding = new TextButton(
                GamePreferences.bindingName(action), menuStyles.button
            );
            binding.addListener(change(() -> beginBindingCapture(action)));
            pauseBindingButtons.put(action, binding);
            panel.add(binding).width(200f).height(30f).pad(1f); panel.row();
        }
        Label hint = new Label(
            "Choose a binding, then press a key or mouse button. Esc cancels.",
            menuStyles.label
        );
        panel.add(hint).colspan(2).padTop(12f).padBottom(14f); panel.row();
        Table buttons = new Table();
        TextButton defaults = new TextButton("RESTORE DEFAULTS", menuStyles.button);
        defaults.addListener(change(() -> {
            GamePreferences.resetBindings();
            refreshPauseBindingLabels();
        }));
        TextButton back = new TextButton("BACK", menuStyles.button);
        back.addListener(change(this::showPauseSettings));
        buttons.add(defaults).width(210f).height(44f).padRight(12f);
        buttons.add(back).width(160f).height(44f);
        panel.add(buttons).colspan(2);
    }

    private void beginBindingCapture(Action action) {
        cancelBindingCapture();
        waitingForBinding = action;
        pauseBindingButtons.get(action).setText("PRESS INPUT...");
    }

    private void cancelBindingCapture() {
        if (waitingForBinding != null) {
            pauseBindingButtons.get(waitingForBinding).setText(
                GamePreferences.bindingName(waitingForBinding)
            );
        }
        waitingForBinding = null;
    }

    private void applyBinding(int code, boolean mouse) {
        Action action = waitingForBinding;
        waitingForBinding = null;
        GamePreferences.setBinding(action, code, mouse);
        refreshPauseBindingLabels();
    }

    private void refreshPauseBindingLabels() {
        for (Action action : Action.values()) {
            pauseBindingButtons.get(action).setText(GamePreferences.bindingName(action));
        }
    }

    private Table modalPanel(String titleText) {
        Table panel = new Table();
        panel.setBackground(menuStyles.panel);
        panel.pad(30f);
        Label title = new Label(titleText, menuStyles.title);
        title.setFontScale(1.5f);
        panel.add(title).colspan(2).padBottom(28f);
        panel.row();
        return panel;
    }

    public void closePauseMenu() {
        paused = false;
        gameOver = false;
        removeOverlay();
    }

    private void removeOverlay() {
        waitingForBinding = null;
        if (pauseOverlay != null) {
            pauseOverlay.remove();
            pauseOverlay = null;
        }
    }

    private ChangeListener change(final Runnable action) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { action.run(); }
        };
    }

    public void updateBoss(
        BossComponent boss,
        HealthComponent health,
        boolean victory
    ) {
        if (boss == null || health == null) {
            bossPanel.setVisible(false);
            return;
        }
        bossPanel.setVisible(true);
        bossLabel.setText(
            victory
                ? "IRHOS DEFEATED"
                : boss.phase.displayName + (boss.isTransitioning() ? " - TRANSFORMING" : "")
        );
        float ratio = health.maximum <= 0f ? 0f : health.current / health.maximum;
        bossHealthBar.setValue(ratio);
    }

    public void update(
        HealthComponent health,
        RunInventoryComponent inventory,
        RelicInventoryComponent relics,
        StatusEffectComponent status,
        DashComponent dash,
        AttackComponent attack,
        DefenseComponent defense,
        int currentLevel,
        int maximumLevel,
        boolean levelComplete,
        int checkpointReached,
        String themeName
    ) {
        float healthRatio = health.maximum <= 0f ? 0f : health.current / health.maximum;
        healthBar.setValue(healthRatio);
        healthLabel.setText(
            "HP " + Math.round(health.current) + "/" + Math.round(health.maximum)
        );
        coinsLabel.setText("Devil Coins: " + inventory.devilCoins);
        relicsLabel.setText("Relics: " + relics.total());
        if (status.isStunned()) {
            statusLabel.setText("STATUS: STUNNED");
        } else if (status.burningTime > 0f && status.isSlowed()) {
            statusLabel.setText("STATUS: BURNING + SLOWED");
        } else if (status.burningTime > 0f) {
            statusLabel.setText("STATUS: BURNING");
        } else if (status.poisonTime > 0f) {
            statusLabel.setText("STATUS: POISONED");
        } else if (status.isSlowed()) {
            statusLabel.setText("STATUS: SLOWED");
        } else {
            statusLabel.setText("");
        }
        statusLabel.setColor(
            statusLabel.getText().length() == 0 ? Color.WHITE : Color.ORANGE
        );
        levelLabel.setText(
            "Level " + currentLevel + "/" + maximumLevel + " - " + themeName
        );

        if (dash.cooldownRemaining <= 0f) {
            dashLabel.setText("Dash: READY");
            dashLabel.setColor(0.25f, 1f, 0.45f, 1f);
        } else {
            dashLabel.setText(
                "Dash: " + String.format("%.1fs", dash.cooldownRemaining)
            );
            dashLabel.setColor(Color.LIGHT_GRAY);
        }

        if (attack.comboStep >= 0 && attack.comboResetRemaining > 0f) {
            comboLabel.setText("Combo: " + (attack.comboStep + 1) + "/3");
        } else {
            comboLabel.setText("Combo: -");
        }

        if (levelComplete) {
            if (checkpointReached == 2) {
                defenseLabel.setText("FINAL CHECKPOINT - PRESS ENTER");
            } else if (checkpointReached == 1) {
                defenseLabel.setText("CHECKPOINT 1 REACHED - PRESS ENTER");
            } else {
                defenseLabel.setText(
                    currentLevel >= maximumLevel
                        ? "BOSS AWAITS - PRESS ENTER"
                        : "LEVEL COMPLETE - PRESS ENTER"
                );
            }
            defenseLabel.setColor(1f, 0.78f, 0.05f, 1f);
        } else if (defense.feedbackTimeRemaining > 0f) {
            defenseLabel.setText("PERFECT PARRY");
            defenseLabel.setColor(0.25f, 1f, 0.4f, 1f);
        } else if (defense.isParryActive()) {
            defenseLabel.setText("PARRY WINDOW");
            defenseLabel.setColor(1f, 0.9f, 0.15f, 1f);
        } else if (defense.blocking) {
            defenseLabel.setText("BLOCKING");
            defenseLabel.setColor(0.75f, 0.45f, 1f, 1f);
        } else {
            defenseLabel.setText("");
        }
    }

    public void render(float deltaTime) {
        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        panelTexture.dispose();
        healthBackgroundTexture.dispose();
        healthFillTexture.dispose();
        menuStyles.dispose();
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
