package com.github.shahamatirtisham.promise_beneath_the_storm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.shahamatirtisham.promise_beneath_the_storm.Main;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RelicType;

/** Presents one free, permanent-for-run boon between dungeon levels. */
public class LevelUpgradeScreen extends BaseMenuScreen {
    private final GameScreen run;
    private boolean selected;

    public LevelUpgradeScreen(final Main game, final GameScreen run) {
        super(game, "CHOOSE A STORM BOON");
        this.run = run;
        addLabel("One choice. It lasts for the rest of this run.");
        addUpgradeButton(1, RelicType.IRON_HEART);
        addUpgradeButton(2, RelicType.STORM_EDGE);
        addUpgradeButton(3, RelicType.WINDSTEP_SIGIL);
        addLabel("Press 1, 2, or 3 — or click an option.");
    }

    private void addUpgradeButton(int number, final RelicType type) {
        addButton(
            number + ". " + type.displayName + " — " + type.description,
            () -> choose(type)
        );
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
