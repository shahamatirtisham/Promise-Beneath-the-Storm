package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import java.util.EnumMap;
import java.util.Map;

/** One template selected and positioned in a generated dungeon layout. */
public class GeneratedRoom {
    public final int id;
    public final String templatePath;
    public final GridPosition position;
    public final Map<GridDirection, Integer> connections =
        new EnumMap<>(GridDirection.class);
    public boolean start;
    public boolean exit;

    public GeneratedRoom(int id, String templatePath, GridPosition position) {
        this.id = id;
        this.templatePath = templatePath;
        this.position = position;
    }
}
