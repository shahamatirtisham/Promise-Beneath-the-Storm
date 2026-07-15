package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** One merchant offer that can be purchased once during the run. */
public class MerchantComponent implements Component {
    public final int cost;
    public boolean purchased;

    public MerchantComponent(int cost) {
        this.cost = cost;
    }
}
