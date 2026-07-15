package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Runtime-ready room metadata extracted from one Tiled map. */
public class RoomDefinition {
    public final String id;
    public final String type;
    public final float width;
    public final float height;
    public final Vector2 playerSpawn;
    public final Vector2 enemySpawn;
    public final Rectangle exitDoor;
    public final Array<Rectangle> collisionRectangles;

    public RoomDefinition(
        String id,
        String type,
        float width,
        float height,
        Vector2 playerSpawn,
        Vector2 enemySpawn,
        Rectangle exitDoor,
        Array<Rectangle> collisionRectangles
    ) {
        this.id = id;
        this.type = type;
        this.width = width;
        this.height = height;
        this.playerSpawn = playerSpawn;
        this.enemySpawn = enemySpawn;
        this.exitDoor = exitDoor;
        this.collisionRectangles = collisionRectangles;
    }
}
