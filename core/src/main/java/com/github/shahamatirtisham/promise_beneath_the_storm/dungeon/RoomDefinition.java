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
    public final Array<EnemySpawnDefinition> enemySpawns;
    public final Array<Vector2> lootSpawns;
    public final Vector2 merchantSpawn;
    public final Map<GridDirection, Vector2> doorSpawns;
    public final Map<GridDirection, Rectangle> doors;
    public final Array<Rectangle> collisionRectangles;
    public final Array<Rectangle> waterZones;
    public final Array<Rectangle> poisonPools;
    public final Array<Rectangle> spikeTraps;

    public RoomDefinition(
        String id,
        String type,
        float width,
        float height,
        Vector2 playerSpawn,
        Array<EnemySpawnDefinition> enemySpawns,
        Array<Vector2> lootSpawns,
        Vector2 merchantSpawn,
        Map<GridDirection, Vector2> doorSpawns,
        Map<GridDirection, Rectangle> doors,
        Array<Rectangle> collisionRectangles,
        Array<Rectangle> waterZones,
        Array<Rectangle> poisonPools,
        Array<Rectangle> spikeTraps
    ) {
        this.id = id;
        this.type = type;
        this.width = width;
        this.height = height;
        this.playerSpawn = playerSpawn;
        this.enemySpawns = new Array<>(enemySpawns);
        this.lootSpawns = new Array<>(lootSpawns);
        this.merchantSpawn = merchantSpawn;
        this.doorSpawns = new EnumMap<>(doorSpawns);
        this.doors = new EnumMap<>(doors);
        this.collisionRectangles = new Array<>(collisionRectangles);
        this.waterZones = new Array<>(waterZones);
        this.poisonPools = new Array<>(poisonPools);
        this.spikeTraps = new Array<>(spikeTraps);
    }
}
