package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Runtime state for the four-stage Irhos boss encounter. */
public class BossComponent implements Component {
    public enum Phase {
        IRON_FIST("Iron Fist"),
        BURNING_GAUNTLETS("Burning Gauntlets"),
        DEVILS_CROWN("Devil's Crown"),
        IRHOS_REVEALED("Irhos Revealed");

        public final String displayName;

        Phase(String displayName) {
            this.displayName = displayName;
        }
    }

    public Phase phase = Phase.IRON_FIST;
    public float transitionTimeRemaining;

    public boolean isTransitioning() {
        return transitionTimeRemaining > 0f;
    }
}
