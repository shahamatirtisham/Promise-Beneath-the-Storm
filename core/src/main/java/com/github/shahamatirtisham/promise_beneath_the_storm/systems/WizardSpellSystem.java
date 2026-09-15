package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.WizardResources;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import static com.github.shahamatirtisham.promise_beneath_the_storm.components.WizardComponent.*;

/** Independent stationary crystals and homing fireballs, with one-shot impact lifecycles. */
public class WizardSpellSystem extends EntitySystem {
    private final Entity player;
    private final World world;
    private final IntSupplier level;
    private final Supplier<Rectangle> roomBounds;
    private final Array<Entity> effects = new Array<>();
    public WizardSpellSystem(Entity player, World world, IntSupplier level, Supplier<Rectangle> roomBounds) {
        this.player = player;
        this.world = world;
        this.level = level;
        this.roomBounds = roomBounds;
    }
    public void spawnCrystal(float x, float y, float casterX, float casterY, float damage) {
        Entity effect = new Entity();
        effect.add(new PositionComponent(x, y));
        effect.add(new TeamComponent(TeamComponent.Team.ENEMY));
        WizardCrystalComponent crystal = new WizardCrystalComponent();
        crystal.damage = damage; crystal.casterX = casterX; crystal.casterY = casterY;
        effect.add(crystal);
        add(effect);
    }
    public void spawnFireball(float x, float y, float directionX, float directionY, float damage) {
        Entity effect = new Entity();
        effect.add(new PositionComponent(x, y));
        effect.add(new VelocityComponent(directionX * FIREBALL_SPEED, directionY * FIREBALL_SPEED));
        effect.add(new TeamComponent(TeamComponent.Team.ENEMY));
        WizardFireballComponent fireball = new WizardFireballComponent();
        fireball.damage = damage;
        fireball.angleDegrees = MathUtils.atan2(directionY, directionX) * MathUtils.radiansToDegrees;
        effect.add(fireball);
        add(effect);
    }
    private void add(Entity effect) {
        effects.add(effect);
        getEngine().addEntity(effect);
    }
    @Override
    public void update(float deltaTime) {
        for (int i = effects.size - 1; i >= 0; i--) {
            Entity effect = effects.get(i);
            WizardCrystalComponent crystal = effect.getComponent(WizardCrystalComponent.class);
            boolean finished = crystal != null ? updateCrystal(effect, crystal, deltaTime)
                : updateFireball(effect, effect.getComponent(WizardFireballComponent.class), deltaTime);
            if (finished) {
                effects.removeIndex(i);
                getEngine().removeEntity(effect);
            }
        }
    }
    private float advance(Animation<TextureRegion> animation, float time, float delta) {
        int frame = animation.getKeyFrameIndex(time);
        float next = time + delta;
        if (animation.getKeyFrameIndex(next) > frame + 1)
            next = Math.nextUp((frame + 1) * animation.getFrameDuration());
        return next;
    }
    private boolean updateCrystal(Entity effect, WizardCrystalComponent crystal, float delta) {
        if (crystal.justSpawned) { crystal.justSpawned = false; return false; }
        Animation<TextureRegion> animation = WizardResources.crystal();
        crystal.stateTime = advance(animation, crystal.stateTime, delta);
        int frame = animation.getKeyFrameIndex(crystal.stateTime);
        if (!crystal.damageApplied && frame == CRYSTAL_DAMAGE_FRAME) {
            crystal.damageApplied = true; // A miss/immunity also consumes the single damage opportunity.
            PositionComponent p = effect.getComponent(PositionComponent.class);
            PositionComponent target = player.getComponent(PositionComponent.class);
            float dx = target.x - p.x, dy = target.y - p.y;
            float radius = ATTACK01_BLAST_RADIUS + PLAYER_RADIUS;
            if (dx * dx + dy * dy <= radius * radius)
                PlayerImpactDamage.apply(player, crystal.casterX, crystal.casterY, crystal.damage, level.getAsInt());
        }
        boolean finished = animation.isAnimationFinished(crystal.stateTime) && crystal.lastFrameShown;
        if (frame == 9) crystal.lastFrameShown = true;
        return finished;
    }
    private boolean updateFireball(Entity effect, WizardFireballComponent fireball, float delta) {
        if (fireball.justSpawned) { fireball.justSpawned = false; return false; }
        if (fireball.state == WizardFireballComponent.State.IMPACT) {
            Animation<TextureRegion> impact = impactAnimation(fireball);
            fireball.stateTime = advance(impact, fireball.stateTime, delta);
            boolean finished = impact.isAnimationFinished(fireball.stateTime) && fireball.lastFrameShown;
            if (impact.getKeyFrameIndex(fireball.stateTime) == impact.getKeyFrames().length - 1) fireball.lastFrameShown = true;
            return finished;
        }
        // Substeps keep steering/contact/wall ordering reliable during a long render update.
        float remaining = Math.min(delta, fireball.lifetimeRemaining);
        while (remaining > 0f) {
            float step = Math.min(remaining, 1f / 60f);
            remaining -= step;
            if (fly(effect, fireball, step)) break;
        }
        fireball.lifetimeRemaining = Math.max(0f, fireball.lifetimeRemaining - delta);
        return fireball.state == WizardFireballComponent.State.FLYING && fireball.lifetimeRemaining <= 0f;
    }
    private boolean fly(Entity effect, WizardFireballComponent fireball, float delta) {
        PositionComponent position = effect.getComponent(PositionComponent.class);
        PositionComponent target = player.getComponent(PositionComponent.class);
        float dx = target.x - position.x, dy = target.y - position.y;
        steer(fireball, dx, dy, delta);
        VelocityComponent velocity = effect.getComponent(VelocityComponent.class);
        velocity.vx = MathUtils.cosDeg(fireball.angleDegrees) * FIREBALL_SPEED;
        velocity.vy = MathUtils.sinDeg(fireball.angleDegrees) * FIREBALL_SPEED;
        float nextX = position.x + velocity.vx * delta, nextY = position.y + velocity.vy * delta;
        float wall = wallFraction(position.x, position.y, nextX, nextY);
        float contact = contactFraction(position.x, position.y, nextX, nextY, target.x, target.y,
            FIREBALL_RADIUS + PLAYER_RADIUS);
        if (contact <= 1f && contact < wall) {
            float sourceX = position.x, sourceY = position.y;
            position.x += (nextX - position.x) * contact;
            position.y += (nextY - position.y) * contact;
            boolean hit = PlayerImpactDamage.apply(player, sourceX, sourceY, fireball.damage, level.getAsInt());
            if (hit) {
                StatusEffectComponent status = player.getComponent(StatusEffectComponent.class);
                if (status != null) status.stunTime = Math.max(status.stunTime, FIREBALL_STUN_DURATION);
                VelocityComponent movement = player.getComponent(VelocityComponent.class);
                if (movement != null) movement.vx = movement.vy = 0f;
            }
            beginImpact(fireball, velocity, false);
            return true;
        }
        if (wall <= 1f || !roomBounds.get().contains(nextX, nextY)) {
            if (wall <= 1f) {
                position.x += (nextX - position.x) * wall;
                position.y += (nextY - position.y) * wall;
            }
            // Walls dissipate without invoking player damage.
            beginImpact(fireball, velocity, true);
            return true;
        }
        position.x = nextX; position.y = nextY;
        fireball.stateTime += delta;
        return false;
    }
    private Animation<TextureRegion> impactAnimation(WizardFireballComponent fireball) {
        return fireball.wallImpact ? WizardResources.wallImpact() : WizardResources.impact();
    }
    private float angleDifference(float target, float current) {
        return ((target - current + 540f) % 360f) - 180f;
    }
    private void steer(WizardFireballComponent fireball, float dx, float dy, float delta) {
        if (fireball.trackingLost) return; // Keep the final heading; never curl or reacquire.
        float trackingDelta = Math.min(delta, Math.max(0f, FIREBALL_HOMING_DURATION - fireball.trackingTime));
        VelocityComponent movement = player.getComponent(VelocityComponent.class);
        float leadX = movement == null ? 0f : movement.vx * FIREBALL_TARGET_LEAD_TIME;
        float leadY = movement == null ? 0f : movement.vy * FIREBALL_TARGET_LEAD_TIME;
        float desired = MathUtils.atan2(dy + leadY, dx + leadX) * MathUtils.radiansToDegrees;
        fireball.angleDegrees = (fireball.angleDegrees + MathUtils.clamp(
            angleDifference(desired, fireball.angleDegrees),
            -FIREBALL_TURN_RATE * trackingDelta, FIREBALL_TURN_RATE * trackingDelta) + 360f) % 360f;
        fireball.trackingTime = Math.min(FIREBALL_HOMING_DURATION, fireball.trackingTime + trackingDelta);
        if (fireball.trackingTime >= FIREBALL_HOMING_DURATION) fireball.trackingLost = true;
    }
    private void beginImpact(WizardFireballComponent fireball, VelocityComponent velocity, boolean wall) {
        fireball.wallImpact = wall;
        fireball.state = WizardFireballComponent.State.IMPACT;
        fireball.stateTime = 0f;
        fireball.lastFrameShown = false;
        velocity.vx = velocity.vy = 0f;
    }
    private float wallFraction(float x, float y, float nextX, float nextY) {
        final float[] closest = {Float.POSITIVE_INFINITY};
        float dx = nextX - x, dy = nextY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0f) return closest[0];
        float offsetX = -dy / length * FIREBALL_RADIUS;
        float offsetY = dx / length * FIREBALL_RADIUS;
        for (int offset = -1; offset <= 1; offset++) {
            float startX = x + offset * offsetX, startY = y + offset * offsetY;
            world.QueryAABB(fixture -> {
                if (solid(fixture) && fixture.testPoint(startX, startY)) closest[0] = 0f;
                return true;
            }, startX - 0.001f, startY - 0.001f, startX + 0.001f, startY + 0.001f);
            world.rayCast((fixture, point, normal, fraction) -> {
                if (!solid(fixture)) return -1f;
                closest[0] = Math.min(closest[0], fraction);
                return fraction;
            }, startX, startY, nextX + offset * offsetX, nextY + offset * offsetY);
        }
        return closest[0];
    }
    private boolean solid(Fixture fixture) {
        // Ignore sensors, actors, and the Witch-only outer boundary fixtures.
        return !fixture.isSensor() && fixture.getBody().getType() == BodyDef.BodyType.StaticBody
            && (fixture.getFilterData().maskBits & 1) != 0;
    }
    private float contactFraction(float x, float y, float endX, float endY,
        float targetX, float targetY, float radius) {
        float dx = endX - x, dy = endY - y;
        float ox = x - targetX, oy = y - targetY;
        float c = ox * ox + oy * oy - radius * radius;
        if (c <= 0f) return 0f;
        float a = dx * dx + dy * dy;
        float b = 2f * (ox * dx + oy * dy);
        float discriminant = b * b - 4f * a * c;
        if (a == 0f || discriminant < 0f) return Float.POSITIVE_INFINITY;
        float t = (-b - (float) Math.sqrt(discriminant)) / (2f * a);
        return t >= 0f && t <= 1f ? t : Float.POSITIVE_INFINITY;
    }
    public void drawCrystals(SpriteBatch batch) {
        for (Entity effect : effects) {
            WizardCrystalComponent crystal = effect.getComponent(WizardCrystalComponent.class);
            if (crystal == null) continue;
            PositionComponent p = effect.getComponent(PositionComponent.class);
            batch.draw(WizardResources.crystal().getKeyFrame(crystal.stateTime),
                p.x - CRYSTAL_RENDER_SIZE / 2f, p.y - CRYSTAL_RENDER_SIZE / 2f,
                CRYSTAL_RENDER_SIZE, CRYSTAL_RENDER_SIZE);
        }
    }
    public void drawFireballs(SpriteBatch batch) {
        for (Entity effect : effects) {
            WizardFireballComponent fireball = effect.getComponent(WizardFireballComponent.class);
            if (fireball == null) continue;
            PositionComponent p = effect.getComponent(PositionComponent.class);
            TextureRegion frame = (fireball.state == WizardFireballComponent.State.FLYING
                ? WizardResources.flight() : impactAnimation(fireball)).getKeyFrame(fireball.stateTime);
            float half = FIREBALL_RENDER_SIZE / 2f;
            batch.draw(frame, p.x - half, p.y - half, half, half,
                FIREBALL_RENDER_SIZE, FIREBALL_RENDER_SIZE, 1f, 1f, fireball.angleDegrees);
        }
    }
    public void clear() {
        for (Entity effect : effects) getEngine().removeEntity(effect);
        effects.clear();
    }
}
