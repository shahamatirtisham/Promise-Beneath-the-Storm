package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

/** Grows a connected dungeon by attaching new rooms to existing rooms. */
public class RoomAccretionGenerator {
    private static final int MAX_PLACEMENT_ATTEMPTS = 1_000;

    private final List<String> templatePaths;

    public RoomAccretionGenerator(String... templatePaths) {
        if (templatePaths.length == 0) {
            throw new IllegalArgumentException("At least one room template is required");
        }
        this.templatePaths = Arrays.asList(templatePaths);
    }

    public DungeonLayout generate(int targetRoomCount, long seed) {
        if (targetRoomCount < 2) {
            throw new IllegalArgumentException("Dungeon needs at least two rooms");
        }

        Random random = new Random(seed);
        List<GeneratedRoom> rooms = new ArrayList<>();
        Map<GridPosition, GeneratedRoom> occupied = new HashMap<>();

        GeneratedRoom start = new GeneratedRoom(
            0,
            selectTemplate(random),
            new GridPosition(0, 0)
        );
        start.start = true;
        rooms.add(start);
        occupied.put(start.position, start);

        int attempts = 0;
        while (rooms.size() < targetRoomCount && attempts < MAX_PLACEMENT_ATTEMPTS) {
            attempts++;
            GeneratedRoom parent = rooms.get(random.nextInt(rooms.size()));

            List<GridDirection> directions = new ArrayList<>(
                Arrays.asList(GridDirection.values())
            );
            Collections.shuffle(directions, random);

            for (GridDirection direction : directions) {
                GridPosition candidatePosition = parent.position.neighbor(direction);
                if (occupied.containsKey(candidatePosition)) {
                    continue;
                }

                GeneratedRoom child = new GeneratedRoom(
                    rooms.size(),
                    selectTemplate(random),
                    candidatePosition
                );
                parent.connections.put(direction, child.id);
                child.connections.put(direction.opposite(), parent.id);
                rooms.add(child);
                occupied.put(candidatePosition, child);
                break;
            }
        }

        if (rooms.size() != targetRoomCount) {
            throw new IllegalStateException(
                "Could not place " + targetRoomCount + " rooms for seed " + seed
            );
        }

        GeneratedRoom exit = findFarthestRoom(rooms);
        exit.exit = true;

        DungeonLayout layout = new DungeonLayout(seed, rooms);
        layout.validate();
        return layout;
    }

    private String selectTemplate(Random random) {
        return templatePaths.get(random.nextInt(templatePaths.size()));
    }

    private GeneratedRoom findFarthestRoom(List<GeneratedRoom> rooms) {
        Queue<Integer> pending = new ArrayDeque<>();
        Map<Integer, Integer> distances = new HashMap<>();
        pending.add(0);
        distances.put(0, 0);
        GeneratedRoom farthest = rooms.get(0);

        while (!pending.isEmpty()) {
            GeneratedRoom room = rooms.get(pending.remove());
            int nextDistance = distances.get(room.id) + 1;

            for (Integer connectedRoomId : room.connections.values()) {
                if (distances.containsKey(connectedRoomId)) {
                    continue;
                }
                distances.put(connectedRoomId, nextDistance);
                pending.add(connectedRoomId);

                if (nextDistance > distances.get(farthest.id)) {
                    farthest = rooms.get(connectedRoomId);
                }
            }
        }
        return farthest;
    }
}
