package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.Constants;
import java.util.HashMap;
import java.util.Map;

/** Draws authored door objects and seals doorways absent from the generated layout. */
public final class TiledRoomRenderer implements Disposable {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Map<TiledMapTileLayer, TiledMapTileLayer.Cell[][]> originalCells = new HashMap<>();
    private GeneratedRoom room;
    private boolean unlocked;
    private float openingTime;
    private float openingDuration;

    public TiledRoomRenderer(String path) {
        map = new TmxMapLoader().load(path);
        renderer = new OrthogonalTiledMapRenderer(map, 1f / Constants.PPM);
        for (MapLayer layer : map.getLayers()) {
            if (!(layer instanceof TiledMapTileLayer)) continue;
            TiledMapTileLayer tiles = (TiledMapTileLayer) layer;
            TiledMapTileLayer.Cell[][] cells =
                new TiledMapTileLayer.Cell[tiles.getWidth()][tiles.getHeight()];
            for (int x = 0; x < tiles.getWidth(); x++) {
                for (int y = 0; y < tiles.getHeight(); y++) cells[x][y] = tiles.getCell(x, y);
            }
            originalCells.put(tiles, cells);
        }
        for (MapObject object : map.getLayers().get("object layer doors animation").getObjects()) {
            if (!(object instanceof TiledMapTileMapObject)) continue;
            TiledMapTile tile = ((TiledMapTileMapObject) object).getTile();
            if (tile instanceof AnimatedTiledMapTile) {
                int duration = 0;
                for (int interval : ((AnimatedTiledMapTile) tile).getAnimationIntervals()) duration += interval;
                openingDuration = Math.max(openingDuration, duration / 1000f);
            }
        }
    }

    public void enter(GeneratedRoom generatedRoom, RoomDefinition definition, boolean isUnlocked) {
        room = generatedRoom;
        unlocked = isUnlocked;
        openingTime = openingDuration; // Returning to an open room must not replay its animation.
        setExitDoorLayersVisible();
        for (Map.Entry<TiledMapTileLayer, TiledMapTileLayer.Cell[][]> entry : originalCells.entrySet()) {
            TiledMapTileLayer layer = entry.getKey();
            TiledMapTileLayer.Cell[][] cells = entry.getValue();
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) layer.setCell(x, y, cells[x][y]);
            }
            for (GridDirection direction : GridDirection.values()) {
                if (room.connections.containsKey(direction)) continue;
                Rectangle door = definition.doors.get(direction);
                int x0 = Math.round(door.x * Constants.PPM / layer.getTileWidth());
                int y0 = Math.round(door.y * Constants.PPM / layer.getTileHeight());
                int width = Math.round(door.width * Constants.PPM / layer.getTileWidth());
                int height = Math.round(door.height * Constants.PPM / layer.getTileHeight());
                // Continue the neighboring wall's tiles across the unused doorway.
                boolean horizontal = direction == GridDirection.NORTH || direction == GridDirection.SOUTH;
                for (int x = x0; x < x0 + width; x++) {
                    for (int y = y0; y < y0 + height; y++) {
                        layer.setCell(x, y, cells[horizontal ? x - width : x][horizontal ? y : y - height]);
                    }
                }
            }
        }
    }

    public void update(boolean isUnlocked, float delta) {
        if (isUnlocked != unlocked) {
            unlocked = isUnlocked;
            openingTime = 0f;
        } else if (unlocked) {
            openingTime = Math.min(openingDuration, openingTime + delta);
        }
        setExitDoorLayersVisible();
    }

    private void setExitDoorLayersVisible() {
        boolean opening = unlocked && !isPassable();
        setLayerVisible("exit door closed layer", !unlocked);
        setLayerVisible("exit door animation layer", opening);
        setLayerVisible("exit door open layer", unlocked && !opening);
        setLayerVisible("exit door light layer", unlocked);
    }

    private void setLayerVisible(String name, boolean visible) {
        MapLayer layer = map.getLayers().get(name);
        if (layer != null) {
            layer.setVisible(visible);
        }
    }

    public void setBarrelLayerVisible(int barrelIndex, boolean visible) {
        setLayerVisible("barrel 0" + (barrelIndex + 1) + " tile", visible);
    }

    public boolean isPassable() {
        return unlocked && openingTime >= openingDuration;
    }

    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
        String state = !unlocked ? "closed" : isPassable() ? "open" : "animation";
        MapLayer layer = map.getLayers().get("object layer doors " + state);
        Batch batch = renderer.getBatch();
        batch.begin();
        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof TiledMapTileMapObject) || !object.isVisible()) continue;
            boolean connected = false;
            for (GridDirection direction : room.connections.keySet()) {
                if (("door_" + direction.name().toLowerCase(java.util.Locale.ROOT) + "_" + state)
                    .equals(object.getName())) connected = true;
            }
            if (!connected) continue;
            TiledMapTileMapObject door = (TiledMapTileMapObject) object;
            TiledMapTile tile = door.getTile();
            if (tile instanceof AnimatedTiledMapTile) {
                AnimatedTiledMapTile animation = (AnimatedTiledMapTile) tile;
                int frame = frameIndex(animation.getAnimationIntervals(), openingTime);
                tile = animation.getFrameTiles()[frame];
            }
            TextureRegion region = new TextureRegion(tile.getTextureRegion());
            region.flip(door.isFlipHorizontally(), door.isFlipVertically());
            batch.draw(region, door.getX() / Constants.PPM, door.getY() / Constants.PPM,
                0f, 0f, region.getRegionWidth() / Constants.PPM, region.getRegionHeight() / Constants.PPM,
                door.getScaleX(), door.getScaleY(), -door.getRotation());
        }
        batch.end();
    }

    static int frameIndex(int[] intervals, float elapsedSeconds) {
        float remaining = elapsedSeconds * 1000f;
        for (int i = 0; i < intervals.length - 1; i++) {
            if (remaining < intervals[i]) return i;
            remaining -= intervals[i];
        }
        return intervals.length - 1;
    }

    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
