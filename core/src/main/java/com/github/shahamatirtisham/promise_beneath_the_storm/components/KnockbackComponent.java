package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Short movement override applied by impacts such as the combo finisher. */
public class KnockbackComponent implements Component {
    public float timeRemaining;
    public float velocityX;
    public float velocityY;
}
