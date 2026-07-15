package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

/** Cardinal directions used to connect rooms in the dungeon grid. */
public enum GridDirection {
    NORTH(0, 1),
    SOUTH(0, -1),
    EAST(1, 0),
    WEST(-1, 0);

    public final int deltaX;
    public final int deltaY;

    GridDirection(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public GridDirection opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
                throw new IllegalStateException("Unknown direction: " + this);
        }
    }
}
