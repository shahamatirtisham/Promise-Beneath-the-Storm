package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.EnumMap;
import java.util.Map;

/** Runtime-ready room metadata extracted from one Tiled map. */
public class RoomDefinition {
    public final String id;
    public final String type;
    public final float width;
    public final float height;
    public final Vector2 playerSpawn;
    public final Array<Vector2> enemySpawns;
    public final Array<Vector2> lootSpawns;
    public final Map<GridDirection, Vector2> doorSpawns;
    public final Map<GridDirection, Rectangle> doors;
    public final Array<Rectangle> collisionRectangles;

    public RoomDefinition(
        String id,
        String type,
        float width,
        float height,
        Vector2 playerSpawn,
        Array<Vector2> enemySpawns,
        Array<Vector2> lootSpawns,
        Map<GridDirection, Vector2> doorSpawns,
        Map<GridDirection, Rectangle> doors,
        Array<Rectangle> collisionRectangles
    ) {
        this.id = id;
        this.type = type;
        this.width = width;
        this.height = height;
        this.playerSpawn = playerSpawn;
        this.enemySpawns = new Array<>(enemySpawns);
        this.lootSpawns = new Array<>(lootSpawns);
        this.doorSpawns = new EnumMap<>(doorSpawns);
        this.doors = new EnumMap<>(doors);
        this.collisionRectangles = collisionRectangles;
    }
}
