package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Visual/runtime state for Iron Fist circles and tracking crystal strikes. */
public class IronFistEffectComponent implements Component {
    public enum Type { BOSS_CAST_CIRCLE, SKELETON_SUMMON_CIRCLE, WIZARD_STRIKE }

    public final Type type;
    public float elapsed;
    public boolean damageApplied;

    public IronFistEffectComponent(Type type) {
        this.type = type;
    }
}
