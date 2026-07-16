package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import java.util.Objects;

/** Immutable integer coordinate occupied by one generated room. */
public final class GridPosition {
    public final int x;
    public final int y;

    public GridPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public GridPosition neighbor(GridDirection direction) {
        return new GridPosition(x + direction.deltaX, y + direction.deltaY);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GridPosition)) {
            return false;
        }
        GridPosition position = (GridPosition) other;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
d
