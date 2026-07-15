package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State and timing for blocking and perfect parries. */
public class DefenseComponent implements Component {
    public boolean blocking;
    public float parryTimeRemaining;
    public float feedbackTimeRemaining;
    public float parryWindow = 0.15f;
    public float damageReduction = 0.6f;

    public boolean isParryActive() {
        return blocking && parryTimeRemaining > 0f;
    }
}
