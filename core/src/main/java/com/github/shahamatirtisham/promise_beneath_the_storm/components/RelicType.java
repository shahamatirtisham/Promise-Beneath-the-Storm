package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;

/** Small, stackable upgrades that last for the current dungeon run. */
public enum RelicType {
    IRON_HEART("Iron Heart", "+15 maximum HP"),
    STORM_EDGE("Storm Edge", "+3 melee damage"),
    WINDSTEP_SIGIL("Windstep Sigil", "-0.08s dash cooldown");

    public final String displayName;
    public final String description;

    RelicType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public void apply(Entity player) {
        RelicInventoryComponent relics =
            player.getComponent(RelicInventoryComponent.class);
        switch (this) {
            case IRON_HEART:
                relics.ironHeart++;
                HealthComponent health = player.getComponent(HealthComponent.class);
                health.maximum += 15f;
                health.current = Math.min(health.maximum, health.current + 15f);
                break;
            case STORM_EDGE:
                relics.stormEdge++;
                player.getComponent(AttackComponent.class).damage += 3f;
                break;
            case WINDSTEP_SIGIL:
                relics.windstepSigil++;
                DashComponent dash = player.getComponent(DashComponent.class);
                dash.cooldownDuration = Math.max(0.5f, dash.cooldownDuration - 0.08f);
                break;
        }
        Gdx.app.log("Relic", displayName + " acquired");
    }
}
