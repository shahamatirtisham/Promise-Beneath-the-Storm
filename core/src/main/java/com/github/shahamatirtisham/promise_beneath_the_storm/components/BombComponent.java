package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

/** Ground-plane flight and separate visual elevation; explosion reuses this entity. */
public class BombComponent implements Component {
    public final Entity owner;
    public final float startX, startY;
    public float targetX, targetY;
    public float flightTime, height, explosionTime;
    public boolean exploded;

    public BombComponent(Entity owner, float x, float y, float targetX, float targetY) {
        this.owner = owner;
        this.startX = x;
        this.startY = y;
        this.targetX = targetX;
        this.targetY = targetY;
    }
}
