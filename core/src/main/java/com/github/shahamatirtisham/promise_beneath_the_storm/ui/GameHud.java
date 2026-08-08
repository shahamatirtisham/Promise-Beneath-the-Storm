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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.MerchantOfferType;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerRangedComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import java.util.EnumMap;

/** Fixed-screen gameplay information that does not reveal dungeon navigation. */
public class GameHud implements Disposable {
    private static final float GAMEPLAY_MENU_WIDTH = SettingsMenuBuilder.PANEL_WIDTH;
    private static final float GAMEPLAY_MENU_HEIGHT = SettingsMenuBuilder.PANEL_HEIGHT;
    private static final float GAMEPLAY_MENU_BUTTON_WIDTH = 251.875f;
    private static final float GAMEPLAY_MENU_BUTTON_HEIGHT = 45f;

    private enum OverlayView {
        NONE,
        PAUSE,
        SETTINGS,
        CONTROLS,
        GAME_OVER
    }

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
    private final Label merchantLabel;
    private final Label dashLabel;
    private final Label knivesLabel;
    private final Label comboLabel;
    private final Label defenseLabel;
    private final ProgressBar healthBar;
    private final Label bossLabel;
    private final ProgressBar bossHealthBar;
    private final Table bossPanel;
    private final TextButton pauseButton;
    private final MenuStyles menuStyles;
    private final Runnable restartAction;
    private final Runnable mainMenuAction;
    private Table pauseOverlay;
    private boolean paused;
    private boolean gameOver;
    private OverlayView overlayView = OverlayView.NONE;
    private boolean blockNextGameplayFrame;
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
        font = GameFonts.create(16);
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
        merchantLabel = new Label("", labelStyle);
        dashLabel = new Label("", labelStyle);
        knivesLabel = new Label("", labelStyle);
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
        panel.add(knivesLabel).colspan(2);
        panel.row();
        panel.add(comboLabel).colspan(2);
        panel.row();
        panel.add(defenseLabel).colspan(2);
        panel.row();
        panel.add(merchantLabel).colspan(2).width(290f).padTop(7f);

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
        pauseButton = new TextButton("PAUSE", menuStyles.button);
        pauseButton.addListener(change(this::showPauseMenu));
        pauseRoot.add(pauseButton).width(126f).height(46.2f);
        stage.addActor(pauseRoot);
    }

    public Stage getStage() { return stage; }
    public boolean isPaused() { return paused || gameOver; }

    /** True when a gameplay mouse click belongs to the fixed Pause button. */
    public boolean isPointerOverPauseButton() {
        Vector2 local = pauseButton.screenToLocalCoordinates(
            new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );
        return pauseButton.hit(local.x, local.y, true) != null;
    }

    /** Prevents the input that closed a UI overlay from reaching gameplay. */
    public boolean consumeGameplayInputBlock() {
        if (!blockNextGameplayFrame) return false;
        blockNextGameplayFrame = false;
        return true;
    }

    /**
     * Handles one backward navigation step.
     * @return true when the action changed away from the gameplay screen.
     */
    public boolean handleEscape() {
        if (gameOver) {
            mainMenuAction.run();
            return true;
        }
        if (overlayView == OverlayView.CONTROLS) {
            showPauseSettings();
        } else if (overlayView == OverlayView.SETTINGS) {
            showPauseButtons();
        } else if (overlayView == OverlayView.PAUSE) {
            closePauseMenu();
        } else {
            showPauseMenu();
        }
        return false;
    }

    public void setGameOver(boolean dead) {
        if (!dead || gameOver) return;
        gameOver = true;
        paused = false;
        pauseButton.setDisabled(true);
        overlayView = OverlayView.GAME_OVER;
        showGameOverMenu();
    }

    private void showGameOverMenu() {
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("GAME OVER");
        panel.padTop(36f).padBottom(36f);
        pauseOverlay.add(panel)
            .width(GAMEPLAY_MENU_WIDTH)
            .height(GAMEPLAY_MENU_HEIGHT);

        TextButton restart = new TextButton(
            "RESTART", menuStyles.button
        );
        restart.addListener(change(restartAction));
        panel.add(restart)
            .width(GAMEPLAY_MENU_BUTTON_WIDTH)
            .height(GAMEPLAY_MENU_BUTTON_HEIGHT)
            .padBottom(16.8f);
        panel.row();

        TextButton mainMenu = new TextButton("MAIN MENU", menuStyles.button);
        mainMenu.addListener(change(mainMenuAction));
        panel.add(mainMenu)
            .width(GAMEPLAY_MENU_BUTTON_WIDTH)
            .height(GAMEPLAY_MENU_BUTTON_HEIGHT);
    }

    private void showPauseMenu() {
        if (paused) return;
        paused = true;
        showPauseButtons();
    }

    private void showPauseButtons() {
        overlayView = OverlayView.PAUSE;
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("GAME PAUSED");
        panel.padTop(36f).padBottom(36f);
        pauseOverlay.add(panel)
            .width(GAMEPLAY_MENU_WIDTH)
            .height(GAMEPLAY_MENU_HEIGHT);

        TextButton resume = new TextButton("RESUME", menuStyles.button);
        resume.addListener(change(this::closePauseMenu));
        panel.add(resume).width(GAMEPLAY_MENU_BUTTON_WIDTH).height(GAMEPLAY_MENU_BUTTON_HEIGHT).padBottom(16.8f); panel.row();

        TextButton restart = new TextButton("RESTART", menuStyles.button);
        restart.addListener(change(restartAction));
        panel.add(restart).width(GAMEPLAY_MENU_BUTTON_WIDTH).height(GAMEPLAY_MENU_BUTTON_HEIGHT).padBottom(16.8f); panel.row();

        TextButton mainMenu = new TextButton("MAIN MENU", menuStyles.button);
        mainMenu.addListener(change(mainMenuAction));
        panel.add(mainMenu).width(GAMEPLAY_MENU_BUTTON_WIDTH).height(GAMEPLAY_MENU_BUTTON_HEIGHT).padBottom(16.8f); panel.row();

        TextButton settings = new TextButton("SETTINGS", menuStyles.button);
        settings.addListener(change(this::showPauseSettings));
        panel.add(settings).width(GAMEPLAY_MENU_BUTTON_WIDTH).height(GAMEPLAY_MENU_BUTTON_HEIGHT);
    }

    private void showPauseSettings() {
        overlayView = OverlayView.SETTINGS;
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = modalPanel("SETTINGS");
        pauseOverlay.add(panel).width(SettingsMenuBuilder.PANEL_WIDTH);
        SettingsMenuBuilder.populate(
            panel, menuStyles, this::showPauseControls, this::showPauseButtons
        );
        pauseOverlay.getCell(panel).height(SettingsMenuBuilder.PANEL_HEIGHT);
    }

    private void showPauseControls() {
        overlayView = OverlayView.CONTROLS;
        waitingForBinding = null;
        removeOverlay();
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        stage.addActor(pauseOverlay);
        Table panel = ControlsMenuBuilder.createPanel(menuStyles);
        pauseOverlay.add(ControlsMenuBuilder.scrollable(panel))
            .width(ControlsMenuBuilder.PANEL_WIDTH)
            .height(Value.percentHeight(ControlsMenuBuilder.PANEL_HEIGHT_RATIO, pauseOverlay))
            .pad(12f);
        ControlsMenuBuilder.populate(
            panel,
            menuStyles,
            pauseBindingButtons,
            this::beginBindingCapture,
            () -> {
                GamePreferences.resetBindings();
                refreshPauseBindingLabels();
            },
            this::showPauseSettings
        );
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
        pauseButton.setDisabled(false);
        overlayView = OverlayView.NONE;
        blockNextGameplayFrame = true;
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
        PlayerRangedComponent ranged,
        MerchantComponent merchant,
        boolean merchantNearby,
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

        if (merchant != null && merchantNearby) {
            StringBuilder offers = new StringBuilder("MERCHANT STOCK\n");
            for (int index = 0; index < merchant.offerTypes.length; index++) {
                MerchantOfferType type = merchant.offerTypes[index];
                String name = type == MerchantOfferType.KNIFE ? "Knife"
                    : type == MerchantOfferType.KNIFE_POUCH ? "Knife Pouch"
                    : merchant.relicOffers[index].displayName;
                String description = type == MerchantOfferType.KNIFE
                    ? "refills one pouch slot"
                    : type == MerchantOfferType.KNIFE_POUCH
                        ? "+1 knife capacity"
                        : merchant.relicOffers[index].description;
                offers.append(index + 1)
                    .append(". ")
                    .append(name)
                    .append(" (")
                    .append(description)
                    .append(") - ")
                    .append(merchant.costs[index])
                    .append(" coins");
                if (type != MerchantOfferType.KNIFE && merchant.isPurchased(index)) {
                    offers.append(" [SOLD]");
                }
                if (index < merchant.offerTypes.length - 1) {
                    offers.append("\n");
                }
            }
            merchantLabel.setText(offers);
            merchantLabel.setColor(0.9f, 0.55f, 1f, 1f);
        } else {
            merchantLabel.setText("");
        }

        if (dash.cooldownRemaining <= 0f) {
            dashLabel.setText("Dash: READY");
            dashLabel.setColor(0.25f, 1f, 0.45f, 1f);
        } else {
            dashLabel.setText(
                "Dash: " + String.format("%.1fs", dash.cooldownRemaining)
            );
            dashLabel.setColor(Color.LIGHT_GRAY);
        }

        knivesLabel.setText(
            "Knives [Q]: " + ranged.charges + "/" + ranged.maximumCharges
        );

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
