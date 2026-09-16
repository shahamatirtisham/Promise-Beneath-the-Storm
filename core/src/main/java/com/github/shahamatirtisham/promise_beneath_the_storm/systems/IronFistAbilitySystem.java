package com.github.shahamatirtisham.promise_beneath_the_storm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.IronFistFxResources;

/** Owns only the two added Iron Fist abilities and their temporary entities. */
public final class IronFistAbilitySystem extends EntitySystem {
    public static final int WIZARD_STRIKE_COUNT = 4;
    public static final float WIZARD_DAMAGE = 12f;
    public static final float WIZARD_HIT_RADIUS = 0.7f;
    public static final float WIZARD_HIT_TIME = 0.48f;
    public static final float WIZARD_SEQUENCE_COOLDOWN = 5f;
    public static final float SUMMON_COOLDOWN = 8f;
    public static final float SUMMON_SKELETON_APPEAR_TIME = 0.36f;
    public static final float EFFECT_RENDER_SIZE = 2.4f;
    public static final float POST_ABILITY_SLAM_DELAY = 0.75f;

    private final Entity player;
    private final World world;
    private final Array<Entity> enemies;
    private final IronFistFxResources resources;
    private final Array<Entity> effects = new Array<>();
    private final Array<Entity> skeletons = new Array<>();
    private final Vector2[] pendingSkeletonPositions = new Vector2[3];
    private Engine engine;
    private Entity boss;
    private boolean pendingSkeletonCreation;

    public IronFistAbilitySystem(Entity player, World world, Array<Entity> enemies,
        IronFistFxResources resources) {
        this.player = player;
        this.world = world;
        this.enemies = enemies;
        this.resources = resources;
        for (int i = 0; i < pendingSkeletonPositions.length; i++)
            pendingSkeletonPositions[i] = new Vector2();
    }

    @Override public void addedToEngine(Engine engine) {
        this.engine = engine;
    }

    @Override public void update(float delta) {
        findBoss();
        updateEffects(delta);
        removeMissingSkeletonReferences();
        if (boss == null) return;
        BossComponent data = boss.getComponent(BossComponent.class);
        if (data.phase != BossComponent.Phase.IRON_FIST || data.isTransitioning()) {
            cleanupIronFistEntities();
            return;
        }
        if (boss.getComponent(HealthComponent.class).current <= 0f) {
            cleanupIronFistEntities();
            return;
        }

        data.wizardCooldownRemaining = Math.max(0f, data.wizardCooldownRemaining - delta);
        if (!data.skeletonGroupActive)
            data.summonCooldownRemaining = Math.max(0f, data.summonCooldownRemaining - delta);

        switch (data.attackState) {
            case IRON_WIZARD_CAST:
                updateWizardCast(data, delta);
                return;
            case IRON_WIZARD_SEQUENCE:
                updateWizardSequence(data);
                return;
            case IRON_SKELETON_SUMMON:
                updateSkeletonSummon(data, delta);
                return;
            default:
                break;
        }

        if (data.skeletonGroupActive && skeletons.size == 0 && !pendingSkeletonCreation) {
            data.skeletonGroupActive = false;
            data.summonCooldownRemaining = SUMMON_COOLDOWN;
        }
        if (data.attackState != BossComponent.AttackState.PURSUIT) return;
        if (data.summonCooldownRemaining <= 0f && !data.skeletonGroupActive) {
            beginSkeletonSummon();
        } else if (data.wizardCooldownRemaining <= 0f) {
            beginWizardCast();
        }
    }

    /** TEMPORARY DEVELOPMENT hook used by key 6; this is the production summon path. */
    public boolean requestDebugSkeletonSummon() {
        findBoss();
        if (boss == null) return false;
        BossComponent data = boss.getComponent(BossComponent.class);
        if (data.phase != BossComponent.Phase.IRON_FIST || data.isTransitioning()
            || data.skeletonGroupActive || pendingSkeletonCreation
            || data.attackState != BossComponent.AttackState.PURSUIT) return false;
        beginSkeletonSummon();
        return true;
    }

    private void beginWizardCast() {
        BossComponent data = boss.getComponent(BossComponent.class);
        data.attackState = BossComponent.AttackState.IRON_WIZARD_CAST;
        data.ironAbilityTimeRemaining = resources.summonCircle.getAnimationDuration();
        data.wizardStrikesGenerated = 0;
        zeroBossVelocity();
        PositionComponent position = boss.getComponent(PositionComponent.class);
        createEffect(IronFistEffectComponent.Type.BOSS_CAST_CIRCLE, position.x, position.y);
        Gdx.app.log("Boss", "Iron Fist tracking magic cast started");
    }

    private void updateWizardCast(BossComponent data, float delta) {
        zeroBossVelocity();
        data.ironAbilityTimeRemaining -= delta;
        if (data.ironAbilityTimeRemaining <= 0f) {
            removeEffects(IronFistEffectComponent.Type.BOSS_CAST_CIRCLE);
            data.attackState = BossComponent.AttackState.IRON_WIZARD_SEQUENCE;
            generateWizardStrike(data);
        }
    }

    private void updateWizardSequence(BossComponent data) {
        zeroBossVelocity();
        if (hasEffect(IronFistEffectComponent.Type.WIZARD_STRIKE)) return;
        if (data.wizardStrikesGenerated < WIZARD_STRIKE_COUNT) {
            generateWizardStrike(data);
            return;
        }
        data.wizardCooldownRemaining = WIZARD_SEQUENCE_COOLDOWN;
        finishAbility(data);
        Gdx.app.log("Boss", "Iron Fist tracking magic cooldown started");
    }

    private void generateWizardStrike(BossComponent data) {
        PositionComponent target = player.getComponent(PositionComponent.class);
        createEffect(IronFistEffectComponent.Type.WIZARD_STRIKE, target.x, target.y);
        data.wizardStrikesGenerated++;
        Gdx.app.log("Boss", "Tracking strike " + data.wizardStrikesGenerated + "/4 at "
            + target.x + ", " + target.y);
    }

    private void beginSkeletonSummon() {
        BossComponent data = boss.getComponent(BossComponent.class);
        data.attackState = BossComponent.AttackState.IRON_SKELETON_SUMMON;
        data.ironAbilityTimeRemaining = SUMMON_SKELETON_APPEAR_TIME
            + resources.skeletonSummon.getAnimationDuration();
        data.skeletonGroupActive = true;
        pendingSkeletonCreation = true;
        zeroBossVelocity();
        PositionComponent origin = boss.getComponent(PositionComponent.class);
        for (int i = 0; i < 3; i++) {
            float angle = (float) (-Math.PI / 2.0 + i * Math.PI * 2.0 / 3.0);
            float x = origin.x + (float) Math.cos(angle) * SkeletonComponent.SUMMON_SPACING;
            float y = origin.y + (float) Math.sin(angle) * SkeletonComponent.SUMMON_SPACING;
            pendingSkeletonPositions[i].set(x, y);
            createEffect(IronFistEffectComponent.Type.SKELETON_SUMMON_CIRCLE, x, y);
        }
        Gdx.app.log("Boss", "Iron Fist summoning three skeletons");
    }

    private void updateSkeletonSummon(BossComponent data, float delta) {
        zeroBossVelocity();
        data.ironAbilityTimeRemaining -= delta;
        float elapsed = SUMMON_SKELETON_APPEAR_TIME
            + resources.skeletonSummon.getAnimationDuration()
            - data.ironAbilityTimeRemaining;
        if (pendingSkeletonCreation && elapsed >= SUMMON_SKELETON_APPEAR_TIME) {
            pendingSkeletonCreation = false;
            for (Vector2 position : pendingSkeletonPositions) spawnSkeleton(position);
        }
        if (data.ironAbilityTimeRemaining <= 0f) {
            removeEffects(IronFistEffectComponent.Type.SKELETON_SUMMON_CIRCLE);
            finishAbility(data);
        }
    }

    private void spawnSkeleton(Vector2 position) {
        Entity skeleton = EnemyFactory.createIronFistSkeleton(world, position, resources);
        skeletons.add(skeleton);
        enemies.add(skeleton);
        engine.addEntity(skeleton);
    }

    private void finishAbility(BossComponent data) {
        data.attackState = BossComponent.AttackState.PURSUIT;
        data.attackTimeRemaining = POST_ABILITY_SLAM_DELAY;
        boss.getComponent(EnemyAIComponent.class).state = EnemyAIComponent.State.CHASE;
    }

    private void updateEffects(float delta) {
        for (int i = effects.size - 1; i >= 0; i--) {
            Entity entity = effects.get(i);
            IronFistEffectComponent effect = entity.getComponent(IronFistEffectComponent.class);
            effect.elapsed += delta;
            Animation<TextureRegion> animation = animation(effect.type);
            if (effect.type == IronFistEffectComponent.Type.WIZARD_STRIKE
                && !effect.damageApplied && effect.elapsed >= WIZARD_HIT_TIME) {
                effect.damageApplied = true;
                applyWizardDamage(entity.getComponent(PositionComponent.class));
            }
            if (animation.isAnimationFinished(effect.elapsed)) {
                effects.removeIndex(i);
                engine.removeEntity(entity);
            }
        }
    }

    private void applyWizardDamage(PositionComponent strike) {
        PositionComponent playerPosition = player.getComponent(PositionComponent.class);
        float dx = playerPosition.x - strike.x;
        float dy = playerPosition.y - strike.y;
        if (dx * dx + dy * dy <= WIZARD_HIT_RADIUS * WIZARD_HIT_RADIUS)
            PlayerImpactDamage.apply(player, strike.x, strike.y, WIZARD_DAMAGE, 1);
    }

    private Entity createEffect(IronFistEffectComponent.Type type, float x, float y) {
        Entity effect = new Entity();
        effect.add(new PositionComponent(x, y));
        effect.add(new IronFistEffectComponent(type));
        effects.add(effect);
        engine.addEntity(effect);
        return effect;
    }

    public void drawUnderlays(SpriteBatch batch) {
        for (Entity entity : effects) {
            PositionComponent position = entity.getComponent(PositionComponent.class);
            IronFistEffectComponent effect = entity.getComponent(IronFistEffectComponent.class);
            TextureRegion frame = animation(effect.type).getKeyFrame(effect.elapsed, false);
            float size = effect.type == IronFistEffectComponent.Type.WIZARD_STRIKE
                ? EFFECT_RENDER_SIZE : EFFECT_RENDER_SIZE * 0.9f;
            batch.draw(frame, position.x - size / 2f, position.y - size / 2f, size, size);
        }
    }

    public void drawDebug(ShapeRenderer shapes) {
        shapes.setColor(0.15f, 1f, 1f, 1f);
        for (Entity entity : skeletons) {
            SkeletonComponent skeleton = entity.getComponent(SkeletonComponent.class);
            if (skeleton == null) continue;
            PositionComponent position = entity.getComponent(PositionComponent.class);
            shapes.circle(position.x, position.y, SkeletonComponent.BODY_RADIUS, 32);
            EnemyAIComponent ai = entity.getComponent(EnemyAIComponent.class);
            if (ai.state == EnemyAIComponent.State.ATTACK) {
                shapes.setColor(1f, 0.45f, 0.05f, 1f);
                shapes.circle(position.x, position.y, ai.attackRange + 0.4f, 32);
                shapes.setColor(0.15f, 1f, 1f, 1f);
            }
        }
        shapes.setColor(0.2f, 0.65f, 1f, 1f);
        for (Entity entity : effects) {
            IronFistEffectComponent effect = entity.getComponent(IronFistEffectComponent.class);
            if (effect.type != IronFistEffectComponent.Type.WIZARD_STRIKE) continue;
            PositionComponent position = entity.getComponent(PositionComponent.class);
            shapes.circle(position.x, position.y, WIZARD_HIT_RADIUS, 32);
        }
    }

    public void clear() {
        for (Entity effect : effects) engine.removeEntity(effect);
        effects.clear();
        skeletons.clear();
        pendingSkeletonCreation = false;
        boss = null;
    }

    public void endEncounter() {
        findBoss();
        if (boss != null) cleanupIronFistEntities();
    }

    private void cleanupIronFistEntities() {
        removeEffects(null);
        pendingSkeletonCreation = false;
        for (int i = skeletons.size - 1; i >= 0; i--) {
            Entity skeleton = skeletons.get(i);
            PhysicsComponent physics = skeleton.getComponent(PhysicsComponent.class);
            if (physics != null && physics.body.getWorld() != null) world.destroyBody(physics.body);
            enemies.removeValue(skeleton, true);
            engine.removeEntity(skeleton);
        }
        skeletons.clear();
        BossComponent data = boss.getComponent(BossComponent.class);
        data.skeletonGroupActive = false;
    }

    private void removeMissingSkeletonReferences() {
        for (int i = skeletons.size - 1; i >= 0; i--)
            if (!enemies.contains(skeletons.get(i), true)) skeletons.removeIndex(i);
    }

    private void removeEffects(IronFistEffectComponent.Type type) {
        for (int i = effects.size - 1; i >= 0; i--) {
            Entity effect = effects.get(i);
            if (type != null && effect.getComponent(IronFistEffectComponent.class).type != type) continue;
            effects.removeIndex(i);
            engine.removeEntity(effect);
        }
    }

    private boolean hasEffect(IronFistEffectComponent.Type type) {
        for (Entity effect : effects)
            if (effect.getComponent(IronFistEffectComponent.class).type == type) return true;
        return false;
    }

    private Animation<TextureRegion> animation(IronFistEffectComponent.Type type) {
        return type == IronFistEffectComponent.Type.WIZARD_STRIKE
            ? resources.wizardStrike : resources.summonCircle;
    }

    private void zeroBossVelocity() {
        VelocityComponent velocity = boss.getComponent(VelocityComponent.class);
        velocity.vx = 0f;
        velocity.vy = 0f;
        boss.getComponent(EnemyAIComponent.class).state = EnemyAIComponent.State.ATTACK;
    }

    private void findBoss() {
        if (boss != null && boss.getComponent(BossComponent.class) != null) return;
        boss = null;
        for (Entity enemy : enemies) {
            if (enemy.getComponent(BossComponent.class) != null) {
                boss = enemy;
                break;
            }
        }
    }
}
