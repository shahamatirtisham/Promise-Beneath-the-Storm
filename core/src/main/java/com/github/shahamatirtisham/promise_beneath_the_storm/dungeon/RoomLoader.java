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
            Vector2 enemySpawn = null;
            Rectangle exitDoor = null;
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
                    enemySpawn = worldRectangle.getCenter(new Vector2());
                } else if ("exit_door".equals(name)) {
                    exitDoor = worldRectangle;
                } else if (name != null && name.startsWith("wall_")) {
                    collisions.add(worldRectangle);
                }
            }

            validateRequiredObjects(playerSpawn, enemySpawn, exitDoor, mapPath);
            return new RoomDefinition(
                id,
                type,
                roomWidth,
                roomHeight,
                playerSpawn,
                enemySpawn,
                exitDoor,
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
        Vector2 enemySpawn,
        Rectangle exitDoor,
        String mapPath
    ) {
        if (playerSpawn == null || enemySpawn == null || exitDoor == null) {
            throw new IllegalArgumentException(
                "Room requires player_spawn, enemy_spawn, and exit_door objects: " + mapPath
            );
        }
    }
}
