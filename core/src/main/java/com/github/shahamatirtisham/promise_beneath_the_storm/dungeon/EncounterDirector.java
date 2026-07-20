package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import java.util.Random;

/** Selects deterministic, level-appropriate enemy combinations for a room. */
public final class EncounterDirector {
    private static final EnemySpawnDefinition.Type M = EnemySpawnDefinition.Type.MELEE;
    private static final EnemySpawnDefinition.Type R = EnemySpawnDefinition.Type.RANGED;
    private static final EnemySpawnDefinition.Type H = EnemySpawnDefinition.Type.HEAVY;
    private static final EnemySpawnDefinition.Type C = EnemySpawnDefinition.Type.CHARGER;
    private static final EnemySpawnDefinition.Type N = EnemySpawnDefinition.Type.NECROMANCER;
    private static final EnemySpawnDefinition.Type S = EnemySpawnDefinition.Type.SHIELD_GUARD;

    private EncounterDirector() {
    }

    public static EnemySpawnDefinition.Type[] createRecipe(
        int level,
        RoomType roomType,
        int enemyCount,
        long seed
    ) {
        EnemySpawnDefinition.Type[][] pool = selectPool(level, roomType);
        EnemySpawnDefinition.Type[] selected = pool[new Random(seed).nextInt(pool.length)];
        EnemySpawnDefinition.Type[] result = new EnemySpawnDefinition.Type[enemyCount];
        for (int index = 0; index < enemyCount; index++) {
            result[index] = selected[index % selected.length];
        }
        return result;
    }

    private static EnemySpawnDefinition.Type[][] selectPool(int level, RoomType type) {
        boolean elite = type == RoomType.ELITE;
        if (level <= 1) {
            return elite
                ? new EnemySpawnDefinition.Type[][] {{M, R}, {M, M}}
                : new EnemySpawnDefinition.Type[][] {{M, M, M}, {M, M, R}, {M, R, R}};
        }
        if (level == 2) {
            return elite
                ? new EnemySpawnDefinition.Type[][] {{H, R}, {H, M}}
                : new EnemySpawnDefinition.Type[][] {{M, H, M}, {M, H, R}, {H, R, M}};
        }
        if (level == 3) {
            return elite
                ? new EnemySpawnDefinition.Type[][] {{N, H}, {N, C}}
                : new EnemySpawnDefinition.Type[][] {{N, M, M}, {N, M, R}, {C, M, M}};
        }
        if (level == 4) {
            return elite
                ? new EnemySpawnDefinition.Type[][] {{H, S}, {C, S}}
                : new EnemySpawnDefinition.Type[][] {{S, C, R}, {H, S, M}, {C, M, M}};
        }
        return elite
            ? new EnemySpawnDefinition.Type[][] {{H, C}, {C, S}, {H, S}}
            : level >= 6
                ? new EnemySpawnDefinition.Type[][] {{N, H, S}, {H, C, R}, {S, C, R}}
                : new EnemySpawnDefinition.Type[][] {{H, C, R}, {S, C, R}, {C, R, R}, {H, S, C}};
    }
}
