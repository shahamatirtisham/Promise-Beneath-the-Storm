package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

/** Central configuration for each dungeon level's identity and mechanic. */
public enum LevelTheme {
    STONE_CATACOMBS(1, "Stone Catacombs", Mechanic.NONE, 0.16f, 0.07f, 0.08f),
    FLOODED_TUNNELS(2, "Flooded Tunnels", Mechanic.WATER_ZONES, 0.04f, 0.14f, 0.22f),
    BONE_GRAVEYARD(3, "Bone Graveyard", Mechanic.RESURRECTION, 0.18f, 0.17f, 0.13f),
    DEVILS_WORKSHOP(4, "Devil's Workshop", Mechanic.EXPLOSIVE_BARRELS, 0.24f, 0.1f, 0.03f),
    SHADOW_REALM(5, "Shadow Realm", Mechanic.DARKNESS, 0.035f, 0.025f, 0.08f),
    THRONE_APPROACH(6, "Throne Approach", Mechanic.COMBINED, 0.16f, 0.04f, 0.06f);

    public enum Mechanic {
        NONE,
        WATER_ZONES,
        RESURRECTION,
        EXPLOSIVE_BARRELS,
        DARKNESS,
        COMBINED
    }

    public final int level;
    public final String displayName;
    public final Mechanic mechanic;
    public final float red;
    public final float green;
    public final float blue;

    LevelTheme(
        int level,
        String displayName,
        Mechanic mechanic,
        float red,
        float green,
        float blue
    ) {
        this.level = level;
        this.displayName = displayName;
        this.mechanic = mechanic;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static LevelTheme forLevel(int level) {
        for (LevelTheme theme : values()) {
            if (theme.level == level) {
                return theme;
            }
        }
        throw new IllegalArgumentException("No theme configured for level " + level);
    }
}
