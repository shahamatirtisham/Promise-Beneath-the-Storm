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
import java.util.Comparator;

/** Grows a connected dungeon by attaching new rooms to existing rooms. */
public class RoomAccretionGenerator {
    private static final int MAX_PLACEMENT_ATTEMPTS = 1_000;

    private final List<RoomTemplate> templates;

    public RoomAccretionGenerator(RoomTemplate... templates) {
        if (templates.length == 0) {
            throw new IllegalArgumentException("At least one room template is required");
        }
        this.templates = Arrays.asList(templates);
    }

    public DungeonLayout generate(int targetRoomCount, long seed) {
        if (targetRoomCount < RoomType.values().length) {
            throw new IllegalArgumentException(
                "Dungeon needs at least one room for every required room type"
            );
        }

        Random random = new Random(seed);
        List<GeneratedRoom> rooms = new ArrayList<>();
        Map<GridPosition, GeneratedRoom> occupied = new HashMap<>();

        GeneratedRoom start = new GeneratedRoom(
            0,
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
        assignRoomTypesAndTemplates(rooms, random, exit);

        DungeonLayout layout = new DungeonLayout(seed, rooms);
        layout.validate();
        return layout;
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

    private void assignRoomTypesAndTemplates(
        List<GeneratedRoom> rooms,
        Random random,
        GeneratedRoom exit
    ) {
        Map<Integer, Integer> distances = calculateDistances(rooms);
        List<GeneratedRoom> middleRooms = new ArrayList<>();

        GeneratedRoom start = rooms.get(0);
        assignTypeAndTemplate(start, RoomType.START, random);
        assignTypeAndTemplate(exit, RoomType.EXIT, random);

        for (GeneratedRoom room : rooms) {
            if (room != start && room != exit) {
                middleRooms.add(room);
            }
        }
        middleRooms.sort(Comparator.comparingInt(room -> distances.get(room.id)));

        assignTypeAndTemplate(middleRooms.remove(0), RoomType.COMBAT, random);
        assignTypeAndTemplate(
            middleRooms.remove(middleRooms.size() - 1),
            RoomType.ELITE,
            random
        );

        Collections.shuffle(middleRooms, random);
        assignTypeAndTemplate(middleRooms.remove(0), RoomType.LOOT, random);
        assignTypeAndTemplate(middleRooms.remove(0), RoomType.MERCHANT, random);

        for (GeneratedRoom remaining : middleRooms) {
            assignTypeAndTemplate(remaining, RoomType.COMBAT, random);
        }
    }

    private Map<Integer, Integer> calculateDistances(List<GeneratedRoom> rooms) {
        Queue<Integer> pending = new ArrayDeque<>();
        Map<Integer, Integer> distances = new HashMap<>();
        pending.add(0);
        distances.put(0, 0);

        while (!pending.isEmpty()) {
            GeneratedRoom room = rooms.get(pending.remove());
            for (Integer connectedRoomId : room.connections.values()) {
                if (!distances.containsKey(connectedRoomId)) {
                    distances.put(connectedRoomId, distances.get(room.id) + 1);
                    pending.add(connectedRoomId);
                }
            }
        }
        return distances;
    }

    private void assignTypeAndTemplate(
        GeneratedRoom room,
        RoomType type,
        Random random
    ) {
        List<RoomTemplate> candidates = new ArrayList<>();
        for (RoomTemplate template : templates) {
            if (template.type == type) {
                candidates.add(template);
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No template registered for room type " + type);
        }

        RoomTemplate selected = candidates.get(random.nextInt(candidates.size()));
        room.type = type;
        room.templatePath = selected.path;
    }
}
