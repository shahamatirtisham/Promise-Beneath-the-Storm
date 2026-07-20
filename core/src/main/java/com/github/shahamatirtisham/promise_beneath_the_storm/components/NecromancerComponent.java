package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

/** Controls a ranged enemy's corpse-selection and resurrection ritual. */
public class NecromancerComponent implements Component {
    public Entity targetCorpse;
    public boolean channeling;
    public float channelTimeRemaining;
    public float channelDuration = 2f;
    public float cooldownRemaining;
    public float resurrectionCooldown = 5f;
}
