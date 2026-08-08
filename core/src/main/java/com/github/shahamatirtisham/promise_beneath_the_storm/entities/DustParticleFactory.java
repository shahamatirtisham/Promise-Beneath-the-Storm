package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.DustParticleComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

public final class DustParticleFactory {

    private DustParticleFactory() {
    }

    public static Entity create(Vector2 position) {

        Entity dust = new Entity();

        dust.add(
            new PositionComponent(
                position.x,
                position.y
            )
        );

        dust.add(new DustParticleComponent());

        return dust;
    }
}
