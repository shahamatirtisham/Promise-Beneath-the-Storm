package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** State for an interactable reward chest. */
public class ChestComponent implements Component {
    public final CollectableComponent.Type rewardType;
    public final int rewardValue;
    public boolean unlocked;
    public boolean opened;

    public ChestComponent(CollectableComponent.Type rewardType, int rewardValue) {
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
    }
}
