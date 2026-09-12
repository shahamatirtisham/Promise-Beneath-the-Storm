package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BreakablePotComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.BreakablePotFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.CollectableFactory;
import java.util.Random;
import java.util.function.Consumer;

public class BreakablePotSystem extends IteratingSystem {
    private static final float DEVIL_COIN_DROP_CHANCE = 0.70f;
    private static final int POT_DEVIL_COIN_VALUE = 1;
    private static final int POT_HEAL_VALUE = 20;

    private final ComponentMapper<BreakablePotComponent> potMapper =
        ComponentMapper.getFor(BreakablePotComponent.class);
    private final World world;
    private final Entity player;
    private final Animation<TextureRegion>[] breakAnimations;
    private final Array<Entity> collectables;
    private final Consumer<BreakablePotComponent> onPotBroken;
    private final Random dropRandom = new Random();

    public BreakablePotSystem(
        World world,
        Entity player,
        Animation<TextureRegion>[] breakAnimations,
        Array<Entity> collectables,
        Consumer<BreakablePotComponent> onPotBroken
    ) {
        super(Family.all(
            BreakablePotComponent.class,
            PositionComponent.class
        ).get());
        this.world = world;
        this.player = player;
        this.breakAnimations = breakAnimations;
        this.collectables = collectables;
        this.onPotBroken = onPotBroken;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        BreakablePotComponent pot = potMapper.get(entity);

        if (pot.state == BreakablePotComponent.State.IDLE) {
            tryMeleeHit(entity);
            return;
        }

        pot.stateTime += deltaTime;
        disableCollision(entity);
        Animation<TextureRegion> animation = breakAnimations[pot.variant];
        if (!animation.isAnimationFinished(pot.stateTime)) {
            return;
        }

        PositionComponent position = entity.getComponent(PositionComponent.class);
        if (!pot.dropSpawned) {
            createDrop(position.x, position.y);
            pot.dropSpawned = true;
        }
        getEngine().removeEntity(entity);
    }

    public boolean hit(Entity entity) {
        BreakablePotComponent pot = potMapper.get(entity);
        if (pot == null || pot.state != BreakablePotComponent.State.IDLE) {
            return false;
        }
        pot.state = BreakablePotComponent.State.BREAKING;
        pot.stateTime = 0f;
        onPotBroken.accept(pot);
        disableCollision(entity);
        Gdx.app.log("BreakablePot", "Pot hit; breaking animation started");
        return true;
    }

    private void tryMeleeHit(Entity potEntity) {
        AttackComponent attack = player.getComponent(AttackComponent.class);
        if (!attack.isActive()) {
            return;
        }
        if (AttackHitbox.overlaps(
            player.getComponent(PositionComponent.class),
            player.getComponent(FacingComponent.class),
            attack,
            potEntity.getComponent(PositionComponent.class),
            BreakablePotFactory.HIT_RADIUS
        )) {
            hit(potEntity);
        }
    }

    private void disableCollision(Entity entity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null || world.isLocked()) {
            return;
        }
        world.destroyBody(physics.body);
        entity.remove(PhysicsComponent.class);
    }

    private void createDrop(float x, float y) {
        Vector2 position = new Vector2(x, y);
        Entity drop;
        if (dropRandom.nextFloat() < DEVIL_COIN_DROP_CHANCE) {
            drop = CollectableFactory.createDevilCoins(position, POT_DEVIL_COIN_VALUE);
            Gdx.app.log("BreakablePot", "Pot dropped a Devil Coin");
        } else {
            drop = CollectableFactory.createHeal(position, POT_HEAL_VALUE);
            Gdx.app.log("BreakablePot", "Pot dropped a Health Heart");
        }
        collectables.add(drop);
        getEngine().addEntity(drop);
    }
}
