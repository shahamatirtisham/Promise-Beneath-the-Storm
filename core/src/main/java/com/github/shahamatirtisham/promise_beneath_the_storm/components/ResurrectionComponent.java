package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Allows a marked enemy to revive once after a short vulnerable pause. */
public class ResurrectionComponent implements Component {
    public boolean awaitingResurrection;
    public boolean hasResurrected;
    public boolean beingChanneled;
    public float restoredHealthRatio = 0.5f;
}
