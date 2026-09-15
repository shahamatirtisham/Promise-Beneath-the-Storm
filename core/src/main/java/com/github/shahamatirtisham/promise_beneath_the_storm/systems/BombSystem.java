package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.RoomDefinition;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.BombFactory;
import java.util.function.Supplier;
import static com.github.shahamatirtisham.promise_beneath_the_storm.entities.BombFactory.*;

/** Bombs travel on the floor plane; height affects drawing alone. */
public final class BombSystem extends EntitySystem {
    private final Entity player;
    private final World world;
    private final Supplier<RoomDefinition> room;
    private final BombFactory visuals;
    private final BreakablePotSystem pots;
    private final Array<Entity> active = new Array<>();
    private float cooldown;

    public BombSystem(Entity player, World world, Supplier<RoomDefinition> room,
        BombFactory visuals, BreakablePotSystem pots) {
        this.player = player;
        this.world = world;
        this.room = room;
        this.visuals = visuals;
        this.pots = pots;
    }

    @Override public void update(float dt) {
        cooldown = Math.max(0f, cooldown - dt);
        for (int i = active.size - 1; i >= 0; i--) {
            Entity entity = active.get(i);
            BombComponent bomb = entity.getComponent(BombComponent.class);
            if (bomb.exploded) {
                bomb.explosionTime += dt;
                if (visuals.explosion.isAnimationFinished(bomb.explosionTime)) {
                    getEngine().removeEntity(entity);
                    active.removeIndex(i);
                }
                continue;
            }
            bomb.flightTime = Math.min(BOMB_FLIGHT_DURATION, bomb.flightTime + dt);
            float progress = bomb.flightTime / BOMB_FLIGHT_DURATION;
            PositionComponent position = entity.getComponent(PositionComponent.class);
            position.x = MathUtils.lerp(bomb.startX, bomb.targetX, progress);
            position.y = MathUtils.lerp(bomb.startY, bomb.targetY, progress);
            bomb.height = 4f * BOMB_ARC_HEIGHT * progress * (1f - progress);
            // Recheck live walls: a door that closes during flight is also respected.
            Vector2 safe = landingPoint(world, room.get(), bomb.startX, bomb.startY,
                bomb.targetX, bomb.targetY);
            float traveled = Vector2.dst2(bomb.startX, bomb.startY, position.x, position.y);
            if (traveled >= Vector2.dst2(bomb.startX, bomb.startY, safe.x, safe.y)) {
                position.x = safe.x;
                position.y = safe.y;
                detonate(entity);
            } else if (progress >= 1f) {
                detonate(entity);
            }
        }
        PlayerComponent state = player.getComponent(PlayerComponent.class);
        StatusEffectComponent status = player.getComponent(StatusEffectComponent.class);
        // TEMPORARY BOMB TEST INPUT: minus adds five bombs per key press.
        if (!state.dead && !state.controlsLocked
            && (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT))) {
            state.bombCharges += BOMB_DEBUG_GRANT_AMOUNT;
            Gdx.app.log("Combat", "Test bombs granted. Remaining: " + state.bombCharges);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && cooldown <= 0f
            && state.bombCharges > 0
            && !state.dead && !state.controlsLocked
            && (status == null || !status.isStunned())) {
            throwBomb();
            cooldown = BOMB_THROW_COOLDOWN;
        }
    }

    public Entity throwBomb() {
        PlayerComponent inventory = player.getComponent(PlayerComponent.class);
        if (inventory.bombCharges <= 0) return null;
        PositionComponent start = player.getComponent(PositionComponent.class);
        FacingComponent facing = player.getComponent(FacingComponent.class);
        Vector2 direction = new Vector2(facing.x, facing.y);
        if (direction.isZero()) direction.set(1f, 0f);
        direction.nor().scl(BOMB_THROW_RANGE);
        Vector2 target = landingPoint(world, room.get(), start.x, start.y,
            start.x + direction.x, start.y + direction.y);
        Entity entity = new Entity();
        entity.add(new PositionComponent(start.x, start.y));
        entity.add(new TeamComponent(TeamComponent.Team.PLAYER));
        entity.add(new BombComponent(player, start.x, start.y, target.x, target.y));
        active.add(entity);
        getEngine().addEntity(entity);
        inventory.bombCharges--;
        Gdx.app.log("Combat", "Bomb thrown. Remaining: " + inventory.bombCharges);
        return entity;
    }

    private void detonate(Entity entity) {
        BombComponent bomb = entity.getComponent(BombComponent.class);
        if (bomb.exploded) return;
        bomb.exploded = true;
        bomb.height = 0f;
        bomb.explosionTime = 0f;
        PositionComponent position = entity.getComponent(PositionComponent.class);
        ExplosiveBarrelSystem.triggerBarrelsInBlast(getEngine().getEntitiesFor(
            Family.all(ExplosiveBarrelComponent.class, PositionComponent.class).get()),
            position, BOMB_EXPLOSION_RADIUS, null);
        DamageSystem.applyRadialEnemyDamage(getEngine().getEntitiesFor(
            Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get()),
            position, BOMB_EXPLOSION_RADIUS, BOMB_DAMAGE, true);
        com.badlogic.ashley.utils.ImmutableArray<Entity> targets = getEngine().getEntitiesFor(
            Family.all(BreakablePotComponent.class, PositionComponent.class).get());
        for (int i = 0; i < targets.size(); i++) {
            Entity pot = targets.get(i);
            PositionComponent p = pot.getComponent(PositionComponent.class);
            if (Vector2.dst2(position.x, position.y, p.x, p.y)
                <= BOMB_EXPLOSION_RADIUS * BOMB_EXPLOSION_RADIUS) pots.hit(pot);
        }
    }

    /** Slab intersection against expanded live static polygons. Room fixtures are rectangles;
     * their expanded bounds conservatively protect the bomb footprint even at corners. */
    public static Vector2 landingPoint(World world, RoomDefinition room,
        float x, float y, float targetX, float targetY) {
        float r = BOMB_GROUND_CLEARANCE;
        float dx = targetX - x, dy = targetY - y;
        float fraction = 1f;
        if (dx > 0f) fraction = Math.min(fraction, (room.width - r - x) / dx);
        if (dx < 0f) fraction = Math.min(fraction, (r - x) / dx);
        if (dy > 0f) fraction = Math.min(fraction, (room.height - r - y) / dy);
        if (dy < 0f) fraction = Math.min(fraction, (r - y) / dy);
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        Vector2 vertex = new Vector2();
        for (Body body : bodies) {
            if (!body.isActive() || body.getType() != BodyDef.BodyType.StaticBody) continue;
            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.isSensor() || !(fixture.getShape() instanceof PolygonShape)) continue;
                PolygonShape shape = (PolygonShape) fixture.getShape();
                float left = Float.POSITIVE_INFINITY, bottom = left;
                float right = Float.NEGATIVE_INFINITY, top = right;
                for (int i = 0; i < shape.getVertexCount(); i++) {
                    shape.getVertex(i, vertex);
                    Vector2 v = body.getWorldPoint(vertex);
                    left = Math.min(left, v.x - r); right = Math.max(right, v.x + r);
                    bottom = Math.min(bottom, v.y - r); top = Math.max(top, v.y + r);
                }
                fraction = Math.min(fraction, rectangleEntry(x, y, dx, dy, left, bottom, right, top));
            }
        }
        fraction = MathUtils.clamp(fraction < 1f ? fraction - 0.001f : fraction, 0f, 1f);
        return new Vector2(x + dx * fraction, y + dy * fraction);
    }

    private static float rectangleEntry(float x, float y, float dx, float dy,
        float left, float bottom, float right, float top) {
        float enter = 0f, exit = 1f;
        if (dx == 0f) { if (x < left || x > right) return 1f; }
        else {
            float a = (left - x) / dx, b = (right - x) / dx;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        if (dy == 0f) { if (y < bottom || y > top) return 1f; }
        else {
            float a = (bottom - y) / dy, b = (top - y) / dy;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        return enter <= exit ? enter : 1f;
    }

    public void draw(SpriteBatch batch) {
        for (Entity entity : active) {
            BombComponent bomb = entity.getComponent(BombComponent.class);
            PositionComponent p = entity.getComponent(PositionComponent.class);
            float width = bomb.exploded ? EXPLOSION_RENDER_SIZE : BOMB_RENDER_SIZE;
            float height = width;
            batch.draw(bomb.exploded ? visuals.explosion.getKeyFrame(bomb.explosionTime)
                : visuals.flight.getKeyFrame(bomb.flightTime),
                p.x - width / 2f, p.y + bomb.height - height / 2f, width, height);
        }
    }

    public void clear() {
        for (Entity entity : active) getEngine().removeEntity(entity);
        active.clear();
        cooldown = 0f;
    }
}
