package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Stack counts for relics collected during the current run. */
public class RelicInventoryComponent implements Component {
    public int ironHeart;
    public int stormEdge;
    public int windstepSigil;

    public int total() {
        return ironHeart + stormEdge + windstepSigil;
    }
}
