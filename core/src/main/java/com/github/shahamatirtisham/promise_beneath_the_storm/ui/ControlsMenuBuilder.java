package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences;
import com.github.shahamatirtisham.promise_beneath_the_storm.state.GamePreferences.Action;
import java.util.EnumMap;
import java.util.function.Consumer;

/** Builds the canonical controls layout used from both menu entry points. */
public final class ControlsMenuBuilder {
    public static final float PANEL_WIDTH = 910f;
    public static final float PANEL_HEIGHT_RATIO = 0.805f;
    private static final float BINDING_BUTTON_WIDTH = 249.6f;
    private static final float BINDING_BUTTON_HEIGHT = 45.36f;

    private ControlsMenuBuilder() {}

    public static Table createPanel(MenuStyles styles) {
        Table panel = new Table();
        panel.setBackground(styles.panel);
        panel.pad(30f);
        panel.padTop(36f).padBottom(36f);
        return panel;
    }

    public static ScrollPane scrollable(Table panel) {
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        return scroll;
    }

    public static void populate(
        Table panel,
        MenuStyles styles,
        EnumMap<Action, TextButton> bindingButtons,
        Consumer<Action> bindingAction,
        Runnable restoreDefaults,
        Runnable backAction
    ) {
        Label title = new Label("CONTROLS", styles.title);
        title.setFontScale(1.5f);
        panel.add(title).padBottom(33.6f);
        panel.row();

        Table bindings = new Table();
        bindings.add(new Label("ACTION", styles.label))
            .left()
            .width(BINDING_BUTTON_WIDTH)
            .padRight(24f)
            .padBottom(12f);
        Label bindingHeader = new Label("BINDING", styles.label);
        bindingHeader.setAlignment(Align.center);
        bindings.add(bindingHeader)
            .width(BINDING_BUTTON_WIDTH)
            .padBottom(12f);
        bindings.row();

        bindingButtons.clear();
        for (Action action : Action.values()) {
            bindings.add(new Label(action.label, styles.label))
                .left()
                .width(BINDING_BUTTON_WIDTH)
                .padTop(3f)
                .padBottom(3f)
                .padRight(24f);
            TextButton binding = new TextButton(
                GamePreferences.bindingName(action), styles.button
            );
            binding.addListener(change(() -> bindingAction.accept(action)));
            bindingButtons.put(action, binding);
            bindings.add(binding)
                .width(BINDING_BUTTON_WIDTH)
                .height(BINDING_BUTTON_HEIGHT)
                .padTop(3f)
                .padBottom(3f);
            bindings.row();
        }
        panel.add(bindings);
        panel.row();

        Table actions = new Table();
        TextButton defaults = new TextButton("DEFAULTS", styles.button);
        defaults.addListener(change(restoreDefaults));
        TextButton back = new TextButton("BACK", styles.button);
        back.addListener(change(backAction));
        actions.add(back)
            .width(BINDING_BUTTON_WIDTH)
            .height(BINDING_BUTTON_HEIGHT)
            .padRight(14f);
        actions.add(defaults)
            .width(BINDING_BUTTON_WIDTH)
            .height(BINDING_BUTTON_HEIGHT);
        panel.add(actions).padTop(60f);
    }

    private static ChangeListener change(Runnable action) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { action.run(); }
        };
    }
}
