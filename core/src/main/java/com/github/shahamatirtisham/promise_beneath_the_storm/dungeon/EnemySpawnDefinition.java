package com.github.shahamatirtisham.promise_beneath_the_storm.dungeon;

import com.badlogic.gdx.math.Vector2;

/** Enemy archetype and world position authored on a Tiled spawn object. */
public class EnemySpawnDefinition {
    public enum Type {
        MELEE,
        RANGED,
        HEAVY,
        CHARGER,
        NECROMANCER,
        SHIELD_GUARD
    }

    public final Vector2 position;
    public final Type type;

    public EnemySpawnDefinition(Vector2 position, Type type) {
        this.position = new Vector2(position);
        this.type = type;
    }
}
