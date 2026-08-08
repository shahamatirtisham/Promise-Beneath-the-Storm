package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;

/** Presents one free, permanent-for-run boon between dungeon levels. */
public class LevelUpgradeScreen extends BaseMenuScreen {
    private final GameScreen run;
    private final float upgradeButtonWidth;
    private boolean selected;

    public LevelUpgradeScreen(final Main game, final GameScreen run) {
        super(game, "CHOOSE A STORM BOON");
        this.run = run;
        upgradeButtonWidth = calculateUpgradeButtonWidth();
        addLabel("One choice. It lasts for the rest of this run.");
        addUpgradeButton(1, RelicType.IRON_HEART);
        addUpgradeButton(2, RelicType.STORM_EDGE);
        addUpgradeButton(3, RelicType.WINDSTEP_SIGIL);
    }

    private void addUpgradeButton(int number, final RelicType type) {
        addButton(
            number + ". " + type.displayName + " :: " + type.description,
            () -> choose(type),
            upgradeButtonWidth
        );
    }

    private float calculateUpgradeButtonWidth() {
        GlyphLayout layout = new GlyphLayout();
        float widest = 0f;
        RelicType[] relicTypes = RelicType.values();
        for (int index = 0; index < relicTypes.length; index++) {
            RelicType type = relicTypes[index];
            String text = (index + 1) + ". " + type.displayName + " :: " + type.description;
            layout.setText(styles.font, text);
            widest = Math.max(widest, layout.width);
        }
        return widest * 1.30f;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            choose(RelicType.IRON_HEART);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            choose(RelicType.STORM_EDGE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            choose(RelicType.WINDSTEP_SIGIL);
        }
    }

    private void choose(RelicType type) {
        if (selected) {
            return;
        }
        selected = true;
        game.acceptLevelUpgrade(run, type);
    }
}
