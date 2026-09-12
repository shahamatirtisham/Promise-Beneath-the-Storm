package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HazardComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.StatusEffectComponent;

/** Advances telegraphed room hazards and resolves contact with the player. */
public class HazardSystem extends EntitySystem {
    private final Entity player;
    private final Array<Entity> hazards;

    public HazardSystem(Entity player, Array<Entity> hazards) {
        this.player = player;
        this.hazards = hazards;
    }

    @Override
    public void update(float deltaTime) {
        PositionComponent position = player.getComponent(PositionComponent.class);
        PlayerComponent playerState = player.getComponent(PlayerComponent.class);
        if (playerState.dead) {
            return;
        }

        for (int index = 0; index < hazards.size; index++) {
            HazardComponent hazard = hazards.get(index).getComponent(HazardComponent.class);
            updateCycle(hazard, deltaTime);
            if (!hazard.contains(position.x, position.y)) {
                if (hazard.type == HazardComponent.Type.FIRE) {
                    hazard.hitThisCycle = false;
                }
                continue;
            }

            if (hazard.type == HazardComponent.Type.POISON_POOL) {
                StatusEffectComponent status =
                    player.getComponent(StatusEffectComponent.class);
                status.poisonTime = Math.max(status.poisonTime, 2f);
            } else if (hazard.active && !hazard.hitThisCycle) {
                applyTriggeredHazard(hazard);
            }
        }
    }

    private void updateCycle(HazardComponent hazard, float deltaTime) {
        if (hazard.type == HazardComponent.Type.POISON_POOL
            || hazard.type == HazardComponent.Type.FIRE) {
            return;
        }

        float period = hazard.type == HazardComponent.Type.SPIKES ? 3.4f : 4.2f;
        float activeStart = hazard.type == HazardComponent.Type.SPIKES ? 2.25f : 2.7f;
        float activeEnd = hazard.type == HazardComponent.Type.SPIKES ? 2.75f : 3.5f;
        float previousTime = hazard.cycleTime;
        hazard.cycleTime = (hazard.cycleTime + deltaTime) % period;
        if (hazard.cycleTime < previousTime) {
            hazard.hitThisCycle = false;
        }
        hazard.active = hazard.cycleTime >= activeStart && hazard.cycleTime < activeEnd;
    }

    private void applyTriggeredHazard(HazardComponent hazard) {
        InvulnerabilityComponent invulnerability =
            player.getComponent(InvulnerabilityComponent.class);
        if (invulnerability.isActive()) {
            return;
        }

        HealthComponent health = player.getComponent(HealthComponent.class);
        StatusEffectComponent status = player.getComponent(StatusEffectComponent.class);
        if (hazard.type == HazardComponent.Type.SPIKES) {
            health.current = Math.max(0f, health.current - 12f);
            status.stunTime = Math.max(status.stunTime, 0.25f);
            Gdx.app.log("Hazard", "Spike trap hit: 12 damage");
        } else {
            health.current = Math.max(0f, health.current - 8f);
            status.burningTime = Math.max(status.burningTime, 4f);
            Gdx.app.log(
                "Hazard",
                hazard.type == HazardComponent.Type.FIRE
                    ? "Fire hit: 8 damage + burning"
                    : "Fire vent hit: 8 damage + burning"
            );
        }
        invulnerability.timeRemaining = invulnerability.duration;
        hazard.hitThisCycle = true;
    }
}
