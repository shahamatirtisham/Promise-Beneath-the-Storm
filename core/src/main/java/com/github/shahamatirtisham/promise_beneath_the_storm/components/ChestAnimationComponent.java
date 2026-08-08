package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ChestAnimationComponent implements Component {

    // Before lever
    public Animation<TextureRegion> hidden;

    // After lever (closed -> opening -> open)
    public Animation<TextureRegion> normal;

    public float stateTime = 0f;
}
