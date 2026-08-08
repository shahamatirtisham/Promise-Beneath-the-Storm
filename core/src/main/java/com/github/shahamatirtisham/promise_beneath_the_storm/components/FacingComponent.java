package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

public class FacingComponent implements Component {

    public enum Direction {
        DOWN,
        LEFT,
        RIGHT,
        UP
    }

    public float x = 1f;
    public float y = 0f;

    public Direction direction = Direction.RIGHT;
}
