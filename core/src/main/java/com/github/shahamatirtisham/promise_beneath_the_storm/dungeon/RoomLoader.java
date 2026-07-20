package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import java.util.EnumMap;
import java.util.Map;

/** Loads room metadata and collision rectangles from a Tiled TMX file. */
public final class RoomLoader {
    private static final String OBJECT_LAYER_NAME = "Objects";

    private RoomLoader() {
    }

    public static RoomDefinition load(String mapPath) {
        TiledMap map = new TmxMapLoader().load(mapPath);
        try {
            int mapWidthTiles = map.getProperties().get("width", Integer.class);
            int mapHeightTiles = map.getProperties().get("height", Integer.class);
            int tileWidthPixels = map.getProperties().get("tilewidth", Integer.class);
            int tileHeightPixels = map.getProperties().get("tileheight", Integer.class);

            String id = map.getProperties().get("roomId", "unknown", String.class);
            String type = map.getProperties().get("roomType", "COMBAT", String.class);
            float roomWidth = mapWidthTiles * tileWidthPixels / Constants.PPM;
            float roomHeight = mapHeightTiles * tileHeightPixels / Constants.PPM;

            MapLayer objectLayer = map.getLayers().get(OBJECT_LAYER_NAME);
            if (objectLayer == null) {
                throw new IllegalArgumentException(
                    "Tiled map is missing required layer: " + OBJECT_LAYER_NAME
                );
            }

            Vector2 playerSpawn = null;
            Array<EnemySpawnDefinition> enemySpawns = new Array<>();
            Array<Vector2> lootSpawns = new Array<>();
            Vector2 merchantSpawn = null;
            Map<GridDirection, Vector2> doorSpawns =
                new EnumMap<>(GridDirection.class);
            Map<GridDirection, Rectangle> doors =
                new EnumMap<>(GridDirection.class);
            Array<Rectangle> collisions = new Array<>();

            for (MapObject object : objectLayer.getObjects()) {
                if (!(object instanceof RectangleMapObject)) {
                    continue;
                }

                Rectangle worldRectangle = toWorldRectangle(
                    ((RectangleMapObject) object).getRectangle()
                );
                String name = object.getName();

                if ("player_spawn".equals(name)) {
                    playerSpawn = worldRectangle.getCenter(new Vector2());
                } else if ("enemy_spawn".equals(name)) {
                    String enemyType = object.getProperties().get(
                        "enemyType",
                        "MELEE",
                        String.class
                    );
                    enemySpawns.add(new EnemySpawnDefinition(
                        worldRectangle.getCenter(new Vector2()),
                        parseEnemyType(enemyType, mapPath)
                    ));
                } else if ("loot_spawn".equals(name)) {
                    lootSpawns.add(worldRectangle.getCenter(new Vector2()));
                } else if ("merchant_spawn".equals(name)) {
                    merchantSpawn = worldRectangle.getCenter(new Vector2());
                } else if (name != null && name.startsWith("door_")) {
                    doors.put(directionFromObjectName(name, "door_"), worldRectangle);
                } else if (name != null && name.startsWith("spawn_")) {
                    doorSpawns.put(
                        directionFromObjectName(name, "spawn_"),
                        worldRectangle.getCenter(new Vector2())
                    );
                } else if (name != null && name.startsWith("wall_")) {
                    collisions.add(worldRectangle);
                }
            }

            validateRequiredObjects(
                playerSpawn,
                enemySpawns,
                lootSpawns,
                merchantSpawn,
                doorSpawns,
                doors,
                mapPath
            );
            return new RoomDefinition(
                id,
                type,
                roomWidth,
                roomHeight,
                playerSpawn,
                enemySpawns,
                lootSpawns,
                merchantSpawn,
                doorSpawns,
                doors,
                collisions
            );
        } finally {
            map.dispose();
        }
    }

    private static Rectangle toWorldRectangle(Rectangle pixelRectangle) {
        return new Rectangle(
            pixelRectangle.x / Constants.PPM,
            pixelRectangle.y / Constants.PPM,
            pixelRectangle.width / Constants.PPM,
            pixelRectangle.height / Constants.PPM
        );
    }

    private static void validateRequiredObjects(
        Vector2 playerSpawn,
        Array<EnemySpawnDefinition> enemySpawns,
        Array<Vector2> lootSpawns,
        Vector2 merchantSpawn,
        Map<GridDirection, Vector2> doorSpawns,
        Map<GridDirection, Rectangle> doors,
        String mapPath
    ) {
        if (playerSpawn == null
            || enemySpawns.size == 0
            || lootSpawns.size == 0
            || merchantSpawn == null
            || doorSpawns.size() != GridDirection.values().length
            || doors.size() != GridDirection.values().length) {
            throw new IllegalArgumentException(
                "Room requires four directional doors and spawns: " + mapPath
            );
        }
    }

    private static GridDirection directionFromObjectName(String name, String prefix) {
        return GridDirection.valueOf(
            name.substring(prefix.length()).toUpperCase()
        );
    }

    private static EnemySpawnDefinition.Type parseEnemyType(
        String value,
        String mapPath
    ) {
        try {
            return EnemySpawnDefinition.Type.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Unknown enemyType '" + value + "' in " + mapPath
                    + ". Expected MELEE, RANGED, HEAVY, or CHARGER.",
                exception
            );
        }
    }
}
