package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

/** Connects an ECS entity to the Box2D body that owns its world position. */
public class PhysicsComponent implements Component {
    public final Body body;

    public PhysicsComponent(Body body) {
        this.body = body;
    }
}
