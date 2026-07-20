package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ChargerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.EnemyAIComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.HealthComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.InvulnerabilityComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.NecromancerComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.ResurrectionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.RunInventoryComponent;

/** Channels one corpse at a time and finalizes corpses when no Necromancer survives. */
public class NecromancerSystem extends EntitySystem {
    private final Entity player;
    private ImmutableArray<Entity> necromancers;
    private ImmutableArray<Entity> corpses;

    public NecromancerSystem(Entity player) {
        this.player = player;
    }

    @Override
    public void addedToEngine(Engine engine) {
        necromancers = engine.getEntitiesFor(Family.all(
            NecromancerComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class
        ).get());
        corpses = engine.getEntitiesFor(Family.all(
            ResurrectionComponent.class,
            EnemyAIComponent.class,
            HealthComponent.class,
            PhysicsComponent.class,
            InvulnerabilityComponent.class
        ).get());
    }

    @Override
    public void update(float deltaTime) {
        boolean livingNecromancer = false;
        for (Entity necromancer : necromancers) {
            EnemyAIComponent ai = necromancer.getComponent(EnemyAIComponent.class);
            if (ai.state == EnemyAIComponent.State.DEAD) {
                cancelChannel(necromancer.getComponent(NecromancerComponent.class));
                continue;
            }
            livingNecromancer = true;
            updateNecromancer(necromancer, deltaTime);
        }
        if (!livingNecromancer) {
            finalizeUnrevivedCorpses();
        }
    }

    private void updateNecromancer(Entity entity, float deltaTime) {
        NecromancerComponent necromancer = entity.getComponent(NecromancerComponent.class);
        EnemyAIComponent ai = entity.getComponent(EnemyAIComponent.class);
        if (ai.state == EnemyAIComponent.State.STUNNED) {
            cancelChannel(necromancer);
            return;
        }

        necromancer.cooldownRemaining = Math.max(
            0f,
            necromancer.cooldownRemaining - deltaTime
        );
        if (!necromancer.channeling) {
            if (necromancer.cooldownRemaining <= 0f) {
                beginChannel(necromancer);
            }
            return;
        }

        ResurrectionComponent target = getValidTarget(necromancer);
        if (target == null) {
            cancelChannel(necromancer);
            return;
        }
        ai.state = EnemyAIComponent.State.ATTACK;
        necromancer.channelTimeRemaining -= deltaTime;
        if (necromancer.channelTimeRemaining <= 0f) {
            resurrect(necromancer.targetCorpse, target);
            necromancer.targetCorpse = null;
            necromancer.channeling = false;
            necromancer.cooldownRemaining = necromancer.resurrectionCooldown;
            ai.state = EnemyAIComponent.State.CHASE;
        }
    }

    private void beginChannel(NecromancerComponent necromancer) {
        for (Entity corpse : corpses) {
            ResurrectionComponent resurrection =
                corpse.getComponent(ResurrectionComponent.class);
            if (!resurrection.awaitingResurrection
                || resurrection.hasResurrected
                || resurrection.beingChanneled) {
                continue;
            }
            resurrection.beingChanneled = true;
            necromancer.targetCorpse = corpse;
            necromancer.channeling = true;
            necromancer.channelTimeRemaining = necromancer.channelDuration;
            Gdx.app.log("Necromancer", "Resurrection ritual started");
            return;
        }
    }

    private ResurrectionComponent getValidTarget(NecromancerComponent necromancer) {
        if (necromancer.targetCorpse == null) {
            return null;
        }
        ResurrectionComponent target =
            necromancer.targetCorpse.getComponent(ResurrectionComponent.class);
        return target != null && target.awaitingResurrection ? target : null;
    }

    private void resurrect(Entity corpse, ResurrectionComponent resurrection) {
        resurrection.awaitingResurrection = false;
        resurrection.beingChanneled = false;
        resurrection.hasResurrected = true;
        HealthComponent health = corpse.getComponent(HealthComponent.class);
        health.current = health.maximum * resurrection.restoredHealthRatio;
        corpse.getComponent(PhysicsComponent.class).body.setActive(true);
        corpse.getComponent(InvulnerabilityComponent.class).timeRemaining = 0.5f;
        EnemyAIComponent ai = corpse.getComponent(EnemyAIComponent.class);
        ai.state = EnemyAIComponent.State.CHASE;
        ai.stateTimeRemaining = 0f;
        ChargerComponent charger = corpse.getComponent(ChargerComponent.class);
        if (charger != null) {
            charger.state = ChargerComponent.State.APPROACH;
            charger.stateTimeRemaining = 0f;
        }
        Gdx.app.log("Necromancer", "Corpse resurrected at half health");
    }

    private void cancelChannel(NecromancerComponent necromancer) {
        if (necromancer.targetCorpse != null) {
            ResurrectionComponent target =
                necromancer.targetCorpse.getComponent(ResurrectionComponent.class);
            if (target != null) {
                target.beingChanneled = false;
            }
        }
        necromancer.targetCorpse = null;
        necromancer.channeling = false;
        necromancer.channelTimeRemaining = 0f;
        necromancer.cooldownRemaining = Math.max(
            necromancer.cooldownRemaining,
            necromancer.resurrectionCooldown * 0.5f
        );
    }

    private void finalizeUnrevivedCorpses() {
        for (Entity corpse : corpses) {
            ResurrectionComponent resurrection =
                corpse.getComponent(ResurrectionComponent.class);
            if (!resurrection.awaitingResurrection) {
                continue;
            }
            resurrection.awaitingResurrection = false;
            resurrection.beingChanneled = false;
            resurrection.hasResurrected = true;
            player.getComponent(RunInventoryComponent.class).enemiesDefeated++;
        }
    }
}
