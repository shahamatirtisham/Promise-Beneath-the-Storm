package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;

/** Maps dungeon themes to their combat status effect. */
public final class StatusEffectApplicator {
    private StatusEffectApplicator() {
    }

    public static void applyForLevel(Entity target, int level) {
        StatusEffectComponent status = target.getComponent(StatusEffectComponent.class);
        switch (level) {
            case 2:
                status.slowTime = Math.max(status.slowTime, 2.5f);
                Gdx.app.log("Status", "SLOWED");
                break;
            case 3:
                status.poisonTime = Math.max(status.poisonTime, 5f);
                Gdx.app.log("Status", "POISONED");
                break;
            case 4:
                status.burningTime = Math.max(status.burningTime, 4f);
                Gdx.app.log("Status", "BURNING");
                break;
            case 5:
                status.stunTime = Math.max(status.stunTime, 0.45f);
                Gdx.app.log("Status", "STUNNED");
                break;
            case 6:
                status.burningTime = Math.max(status.burningTime, 3f);
                status.slowTime = Math.max(status.slowTime, 1.5f);
                Gdx.app.log("Status", "BURNING + SLOWED");
                break;
            default:
                break;
        }
    }
}
