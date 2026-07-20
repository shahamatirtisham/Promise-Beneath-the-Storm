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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DashComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DefenseComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;

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
    private final Label dashLabel;
    private final Label comboLabel;
    private final Label defenseLabel;
    private final ProgressBar healthBar;
    private final Label bossLabel;
    private final ProgressBar bossHealthBar;
    private final Table bossPanel;

    public GameHud() {
        stage = new Stage(new ScreenViewport());
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
        DashComponent dash,
        AttackComponent attack,
        DefenseComponent defense,
        int currentLevel,
        int maximumLevel,
        boolean levelComplete
    ) {
        float healthRatio = health.maximum <= 0f ? 0f : health.current / health.maximum;
        healthBar.setValue(healthRatio);
        healthLabel.setText(
            "HP " + Math.round(health.current) + "/" + Math.round(health.maximum)
        );
        coinsLabel.setText("Devil Coins: " + inventory.devilCoins);
        levelLabel.setText("Level " + currentLevel + "/" + maximumLevel);

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
            defenseLabel.setText(
                currentLevel >= maximumLevel
                    ? "BOSS AWAITS - PRESS ENTER"
                    : "LEVEL COMPLETE - PRESS ENTER"
            );
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
