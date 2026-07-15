package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Connected room graph produced from a reproducible random seed. */
public class DungeonLayout {
    public final long seed;
    public final List<GeneratedRoom> rooms;

    public DungeonLayout(long seed, List<GeneratedRoom> rooms) {
        this.seed = seed;
        this.rooms = new ArrayList<>(rooms);
    }

    public GeneratedRoom getRoom(int id) {
        return rooms.get(id);
    }

    public void validate() {
        if (rooms.isEmpty()) {
            throw new IllegalStateException("Dungeon cannot be empty");
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> pending = new ArrayDeque<>();
        pending.add(0);
        visited.add(0);

        while (!pending.isEmpty()) {
            GeneratedRoom room = getRoom(pending.remove());
            for (Integer connectedRoomId : room.connections.values()) {
                if (visited.add(connectedRoomId)) {
                    pending.add(connectedRoomId);
                }
            }
        }

        if (visited.size() != rooms.size()) {
            throw new IllegalStateException(
                "Generated dungeon contains unreachable rooms for seed " + seed
            );
        }
    }

    public String toDebugString() {
        int minimumX = 0;
        int maximumX = 0;
        int minimumY = 0;
        int maximumY = 0;

        for (GeneratedRoom room : rooms) {
            minimumX = Math.min(minimumX, room.position.x);
            maximumX = Math.max(maximumX, room.position.x);
            minimumY = Math.min(minimumY, room.position.y);
            maximumY = Math.max(maximumY, room.position.y);
        }

        StringBuilder output = new StringBuilder();
        output.append("Dungeon seed: ").append(seed).append('\n');
        for (int y = maximumY; y >= minimumY; y--) {
            for (int x = minimumX; x <= maximumX; x++) {
                GeneratedRoom room = findRoomAt(x, y);
                if (room == null) {
                    output.append(" . ");
                } else {
                    output.append(' ').append(room.type.debugSymbol).append(' ');
                }
            }
            output.append('\n');
        }
        return output.toString();
    }

    private GeneratedRoom findRoomAt(int x, int y) {
        for (GeneratedRoom room : rooms) {
            if (room.position.x == x && room.position.y == y) {
                return room;
            }
        }
        return null;
    }
}
