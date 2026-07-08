package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.ashley.core.Entity;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.VelocityComponent;

public class EntityFactory {

    public static Entity createPlayer() {

        Entity player = new Entity();

        player.add(new PlayerComponent());
        player.add(new PositionComponent(0, 0));
        player.add(new VelocityComponent());

        return player;
    }
}
