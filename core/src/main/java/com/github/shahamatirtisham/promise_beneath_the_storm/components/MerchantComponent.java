package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** One merchant offer that can be purchased once during the run. */
public class MerchantComponent implements Component {
    public final int[] costs;
    public final RelicType[] offers;
    public boolean purchased;

    public MerchantComponent(RelicType[] offers, int[] costs) {
        this.offers = offers;
        this.costs = costs;
    }
}
