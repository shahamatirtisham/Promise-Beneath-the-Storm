package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

/** Gameplay role assigned to a generated room. */
public enum RoomType {
    START(false, 'S'),
    COMBAT(true, 'C'),
    LOOT(false, 'L'),
    MERCHANT(false, 'M'),
    ELITE(true, 'B'),
    EXIT(true, 'E');

    public final boolean requiresClear;
    public final char debugSymbol;

    RoomType(boolean requiresClear, char debugSymbol) {
        this.requiresClear = requiresClear;
        this.debugSymbol = debugSymbol;
    }
}
