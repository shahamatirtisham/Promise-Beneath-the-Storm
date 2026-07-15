package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

/** Registers a Tiled room file as a candidate for one gameplay role. */
public class RoomTemplate {
    public final String path;
    public final RoomType type;

    public RoomTemplate(String path, RoomType type) {
        this.path = path;
        this.type = type;
    }
}
