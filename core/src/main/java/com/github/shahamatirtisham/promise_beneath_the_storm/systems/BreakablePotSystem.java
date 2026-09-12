package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.AttackComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BreakablePotComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PhysicsComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlaceholderDropComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PositionComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.BreakablePotFactory;

public class BreakablePotSystem extends IteratingSystem {
    private final ComponentMapper<BreakablePotComponent> potMapper =
        ComponentMapper.getFor(BreakablePotComponent.class);
    private final World world;
    private final Entity player;
    private final Animation<TextureRegion>[] breakAnimations;

    public BreakablePotSystem(
        World world,
        Entity player,
        Animation<TextureRegion>[] breakAnimations
    ) {
        super(Family.all(
            BreakablePotComponent.class,
            PositionComponent.class
        ).get());
        this.world = world;
        this.player = player;
        this.breakAnimations = breakAnimations;
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
            createPlaceholderDrop(position.x, position.y);
            pot.dropSpawned = true;
            Gdx.app.log("BreakablePot", "Placeholder drop spawned at "
                + position.x + ", " + position.y);
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

    private void createPlaceholderDrop(float x, float y) {
        Entity dropEntity = new Entity();
        dropEntity.add(new PositionComponent(x, y));
        dropEntity.add(new PlaceholderDropComponent());
        getEngine().addEntity(dropEntity);
    }
}
