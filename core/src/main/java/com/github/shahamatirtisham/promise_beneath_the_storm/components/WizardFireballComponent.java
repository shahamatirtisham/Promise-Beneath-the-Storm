package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Separate from generic projectiles so knives/boss shots retain their behavior. */
public class WizardFireballComponent implements Component {
    public enum State { FLYING, IMPACT }
    public State state = State.FLYING;
    public float stateTime;
    public float lifetimeRemaining = WizardComponent.FIREBALL_MAX_LIFETIME;
    public float damage;
    public float angleDegrees;
    public float trackingTime;
    public boolean trackingLost;
    public boolean wallImpact;
    public boolean lastFrameShown;
    public boolean justSpawned = true;
}
