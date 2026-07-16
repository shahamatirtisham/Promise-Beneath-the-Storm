package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Resources and upgrades that persist while the current dungeon run is active. */
public class RunInventoryComponent implements Component {
    public int devilCoins;
    public int enemiesDefeated;
}
