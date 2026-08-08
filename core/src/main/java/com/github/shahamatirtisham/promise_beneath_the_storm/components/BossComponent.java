package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Runtime state for the four-stage Irhos boss encounter. */
public class BossComponent implements Component {
    public enum AttackState {
        PURSUIT,
        SLAM_WINDUP,
        SLAM_RECOVERY,
        BURNING_PURSUIT,
        FLAME_PUNCH_WINDUP,
        FLAME_PUNCH_DASH,
        FLAME_PUNCH_RECOVERY,
        CROWN_PURSUIT,
        CROWN_WINDUP,
        CROWN_RECOVERY,
        REVEALED_PURSUIT
    }

    public enum Phase {
        IRON_FIST("Iron Fist"),
        BURNING_GAUNTLETS("Burning Gauntlets"),
        DEVILS_CROWN("Devil's Crown"),
        IRHOS_REVEALED("Irhos Revealed");

        public final String displayName;

        Phase(String displayName) {
            this.displayName = displayName;
        }
    }

    public enum RevealedState {
        PURSUIT,
        SLAM_WINDUP,
        SLAM_RECOVERY,
        PUNCH_WINDUP,
        PUNCH_DASH,
        PUNCH_RECOVERY,
        CROWN_WINDUP,
        CROWN_RECOVERY
    }

    public Phase phase = Phase.IRON_FIST;
    public float transitionTimeRemaining;
    public AttackState attackState = AttackState.PURSUIT;
    public float attackTimeRemaining = 1.25f;
    public float telegraphDuration = 1f;
    public float slamTargetX;
    public float slamTargetY;
    public float slamRadius = 2.1f;
    public float slamDamage = 28f;
    public float slamWindup = 1.05f;
    public float slamRecovery = 1.25f;
    public float punchOriginX;
    public float punchOriginY;
    public float punchDirectionX = 1f;
    public float punchDirectionY;
    public float punchTelegraphLength = 5.5f;
    public float punchWindup = 0.45f;
    public float punchDuration = 0.42f;
    public float punchRecovery = 0.7f;
    public float punchSpeed = 9f;
    public float punchDamage = 24f;
    public boolean punchHit;
    public float crownOriginX;
    public float crownOriginY;
    public float crownWindup = 0.8f;
    public float crownRecovery = 0.65f;
    public float crownProjectileSpeed = 5.2f;
    public float crownProjectileDamage = 16f;
    public float crownRotation;
    public int crownSafeGap;
    public int revealedAttackIndex;
    public boolean revealedConfigured;
    public RevealedState revealedState = RevealedState.PURSUIT;
    public float revealedTimeRemaining;
    public boolean attackCycleReady;

    public boolean isTransitioning() {
        return transitionTimeRemaining > 0f;
    }
}
