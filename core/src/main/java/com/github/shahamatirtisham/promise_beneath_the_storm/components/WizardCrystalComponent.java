package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Stationary spell: PositionComponent is copied from the locked cast target. */
public class WizardCrystalComponent implements Component {
    public float stateTime;
    public float damage;
    public float casterX;
    public float casterY;
    public boolean damageApplied;
    public boolean lastFrameShown;
    public boolean justSpawned = true;
}
