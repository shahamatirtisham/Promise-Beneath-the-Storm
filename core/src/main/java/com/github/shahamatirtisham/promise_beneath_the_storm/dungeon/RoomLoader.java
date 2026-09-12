package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
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
            Array<MapLayer> gameplayLayers = new Array<>();
            if (objectLayer != null) {
                gameplayLayers.add(objectLayer);
            } else {
                MapLayer boundaries = map.getLayers().get("object layer doors and walls");
                MapLayer spawns = map.getLayers().get("object layer spawns");
                if (boundaries != null && spawns != null) {
                    gameplayLayers.add(boundaries);
                    gameplayLayers.add(spawns);
                    MapLayer exitDoorObjects = map.getLayers().get("exit door object layer");
                    if (exitDoorObjects != null) {
                        gameplayLayers.add(exitDoorObjects);
                    }
                }
            }
            if (gameplayLayers.size == 0) {
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
            Array<Rectangle> waterZones = new Array<>();
            Array<Rectangle> poisonPools = new Array<>();
            Array<Rectangle> spikeTraps = new Array<>();
            Array<Polygon> fireZones = new Array<>();
            Array<Rectangle> explosiveBarrelBounds = new Array<>();
            Rectangle exitDoor = null;

            Array<MapObject> gameplayObjects = new Array<>();
            for (MapLayer layer : gameplayLayers) {
                for (MapObject object : layer.getObjects()) {
                    gameplayObjects.add(object);
                }
            }
            for (MapObject object : gameplayObjects) {
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
                } else if ("merchant_spawn".equals(name) || "marchant_spawn".equals(name)) {
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
                } else if ("exit_door".equals(name)) {
                    exitDoor = worldRectangle;
                }
            }

            for (MapLayer layer : map.getLayers()) {
                for (MapObject object : layer.getObjects()) {
                    if (object instanceof PolygonMapObject
                        && "fire_damage".equals(object.getName())) {
                        fireZones.add(toWorldPolygon(
                            ((PolygonMapObject) object).getPolygon()
                        ));
                    } else if (object instanceof RectangleMapObject
                        && "water_body".equals(object.getName())) {
                        waterZones.add(toWorldRectangle(
                            ((RectangleMapObject) object).getRectangle()
                        ));
                    } else if (object instanceof RectangleMapObject
                        && "poison_body".equals(object.getName())) {
                        poisonPools.add(toWorldRectangle(
                            ((RectangleMapObject) object).getRectangle()
                        ));
                    } else if (object instanceof RectangleMapObject
                        && "spike_damage".equals(object.getName())) {
                        spikeTraps.add(toWorldRectangle(
                            ((RectangleMapObject) object).getRectangle()
                        ));
                    } else if (object instanceof RectangleMapObject
                        && isExplosiveBarrelObject(object.getName())) {
                        explosiveBarrelBounds.add(toWorldRectangle(
                            ((RectangleMapObject) object).getRectangle()
                        ));
                    }
                }
            }

            validateRequiredObjects(
                playerSpawn,
                enemySpawns,
                lootSpawns,
                merchantSpawn,
                doorSpawns,
                doors,
                exitDoor,
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
                collisions,
                waterZones,
                poisonPools,
                spikeTraps,
                fireZones,
                explosiveBarrelBounds,
                exitDoor
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

    private static Polygon toWorldPolygon(Polygon pixelPolygon) {
        float[] pixelVertices = pixelPolygon.getTransformedVertices();
        float[] worldVertices = new float[pixelVertices.length];
        for (int index = 0; index < pixelVertices.length; index++) {
            worldVertices[index] = pixelVertices[index] / Constants.PPM;
        }
        return new Polygon(worldVertices);
    }

    private static boolean isExplosiveBarrelObject(String name) {
        return "explosive_barrel".equals(name)
            || (name != null && name.startsWith("barrel_") && name.endsWith("_object"));
    }

    private static void validateRequiredObjects(
        Vector2 playerSpawn,
        Array<EnemySpawnDefinition> enemySpawns,
        Array<Vector2> lootSpawns,
        Vector2 merchantSpawn,
        Map<GridDirection, Vector2> doorSpawns,
        Map<GridDirection, Rectangle> doors,
        Rectangle exitDoor,
        String mapPath
    ) {
        boolean exitRoom = mapPath.endsWith("room_exit.tmx");
        if (enemySpawns.size == 0
            || doorSpawns.size() != GridDirection.values().length
            || doors.size() != GridDirection.values().length) {
            throw new IllegalArgumentException(
                "Room requires enemies and four directional doors and spawns: " + mapPath
            );
        }
        if (exitRoom) {
            if (exitDoor == null) {
                throw new IllegalArgumentException(
                    "Exit room requires an exit_door object: " + mapPath
                );
            }
            return;
        }
        if (playerSpawn == null || lootSpawns.size == 0 || merchantSpawn == null) {
            throw new IllegalArgumentException(
                "Non-exit room requires player, loot, and merchant spawns: " + mapPath
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
                    + ". Expected MELEE, RANGED, HEAVY, CHARGER, NECROMANCER, or SHIELD_GUARD.",
                exception
            );
        }
    }
}
