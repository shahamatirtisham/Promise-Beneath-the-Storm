package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State for a one-use room lever. */
public class LeverComponent implements Component {
    public static final float FRAME_DURATION = 0.12f;
    public static final int FRAME_COUNT = 5;
    public boolean pulling;
    public float stateTime;
    public boolean activated;

    public void restoreActivated() {
        activated = true;
        pulling = false;
        stateTime = FRAME_DURATION * FRAME_COUNT;
    }

    public int frameIndex() {
        return activated ? FRAME_COUNT - 1
            : Math.min(FRAME_COUNT - 1, (int)(stateTime / FRAME_DURATION));
    }
}
