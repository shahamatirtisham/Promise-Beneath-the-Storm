package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** A world pickup that grants Devil Coins when touched by the player. */
public class CollectableComponent implements Component {
    public enum Type {
        DEVIL_COINS,
        HEAL,
        MAX_HEALTH,
        RELIC,
        KNIFE
    }

    public final Type type;
    public final int value;
    public final RelicType relicType;
    public final boolean roomReward;
    public final boolean bonusKnife;
    public boolean collected;

    public CollectableComponent(Type type, int value) {
        this(type, value, null, false, false);
    }

    public CollectableComponent(Type type, int value, RelicType relicType) {
        this(type, value, relicType, false, false);
    }

    public CollectableComponent(
        Type type,
        int value,
        RelicType relicType,
        boolean roomReward,
        boolean bonusKnife
    ) {
        this.type = type;
        this.value = value;
        this.relicType = relicType;
        this.roomReward = roomReward;
        this.bonusKnife = bonusKnife;
    }
}
