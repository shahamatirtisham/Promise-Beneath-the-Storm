package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;

/** Advances the boss at 75%, 50%, and 25% health thresholds. */
public class BossPhaseSystem extends IteratingSystem {
    private static final float TRANSITION_DURATION = 1.5f;

    public BossPhaseSystem() {
        super(Family.all(
            BossComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class,
            InvulnerabilityComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity boss, float deltaTime) {
        BossComponent data = boss.getComponent(BossComponent.class);
        if (data.transitionTimeRemaining > 0f) {
            data.transitionTimeRemaining = Math.max(
                0f,
                data.transitionTimeRemaining - deltaTime
            );
        }

        HealthComponent health = boss.getComponent(HealthComponent.class);
        BossComponent.Phase nextPhase = getNextPhase(data.phase, health);
        if (nextPhase == null) {
            return;
        }

        data.phase = nextPhase;
        data.transitionTimeRemaining = TRANSITION_DURATION;
        data.attackCycleReady = false;
        float threshold = getThreshold(nextPhase);
        health.current = Math.max(health.current, health.maximum * threshold);
        boss.getComponent(InvulnerabilityComponent.class).timeRemaining =
            TRANSITION_DURATION;

        EnemyAIComponent ai = boss.getComponent(EnemyAIComponent.class);
        ai.state = EnemyAIComponent.State.STUNNED;
        ai.stateTimeRemaining = TRANSITION_DURATION;
        applyPhaseTuning(ai, nextPhase);
        Gdx.app.log("Boss", "Phase changed: " + nextPhase.displayName);
    }

    private BossComponent.Phase getNextPhase(
        BossComponent.Phase phase,
        HealthComponent health
    ) {
        float ratio = health.current / health.maximum;
        switch (phase) {
            case IRON_FIST:
                return ratio <= 0.75f ? BossComponent.Phase.BURNING_GAUNTLETS : null;
            case BURNING_GAUNTLETS:
                return ratio <= 0.5f ? BossComponent.Phase.DEVILS_CROWN : null;
            case DEVILS_CROWN:
                return ratio <= 0.25f ? BossComponent.Phase.IRHOS_REVEALED : null;
            case IRHOS_REVEALED:
            default:
                return null;
        }
    }

    private float getThreshold(BossComponent.Phase phase) {
        switch (phase) {
            case BURNING_GAUNTLETS: return 0.75f;
            case DEVILS_CROWN: return 0.5f;
            case IRHOS_REVEALED: return 0.25f;
            default: return 1f;
        }
    }

    private void applyPhaseTuning(
        EnemyAIComponent ai,
        BossComponent.Phase phase
    ) {
        switch (phase) {
            case BURNING_GAUNTLETS:
                ai.movementSpeed = 2.1f;
                ai.attackWindup = 0.55f;
                ai.recoveryDuration = 0.75f;
                ai.attackDamage = 24f;
                break;
            case DEVILS_CROWN:
                ai.movementSpeed = 2.4f;
                ai.attackWindup = 0.45f;
                ai.recoveryDuration = 0.65f;
                ai.attackDamage = 28f;
                break;
            case IRHOS_REVEALED:
                ai.movementSpeed = 2.8f;
                ai.attackWindup = 0.35f;
                ai.recoveryDuration = 0.5f;
                ai.attackDamage = 32f;
                break;
            default:
                break;
        }
    }
}
