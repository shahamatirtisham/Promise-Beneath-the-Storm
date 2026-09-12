package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;

/** Shared tapered melee hit area used by enemies and breakable scenery. */
public final class AttackHitbox {
    private static final float START_DISTANCE = 0.35f;

    private AttackHitbox() {
    }

    public static boolean overlaps(
        PositionComponent attacker,
        FacingComponent facing,
        AttackComponent attack,
        PositionComponent target,
        float targetRadius
    ) {
        float deltaX = target.x - attacker.x;
        float deltaY = target.y - attacker.y;
        float forward = deltaX * facing.x + deltaY * facing.y;
        if (forward < START_DISTANCE - targetRadius
            || forward > attack.reach + targetRadius) {
            return false;
        }

        float sideways = Math.abs(deltaX * -facing.y + deltaY * facing.x);
        float progress = Math.max(0f, Math.min(
            1f,
            (forward - START_DISTANCE) / (attack.reach - START_DISTANCE)
        ));
        return sideways <= attack.halfWidth * progress + targetRadius;
    }
}
