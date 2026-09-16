package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.EnemyFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.EnemyAnimationSystem;
import java.lang.reflect.Method;

public final class NecromancerVisualRegressionTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load(); Box2D.init();
        Method setup=ShieldGuardRegressionTest.class.getDeclaredMethod("initializeHeadlessGraphics");
        setup.setAccessible(true); setup.invoke(null);
        World world=new World(new Vector2(),true);
        Entity enemy=EnemyFactory.createNecromancer(world,new Vector2());
        AnimationComponent a=enemy.getComponent(AnimationComponent.class);
        try {
            check(a.sourceTexture.getWidth()==1300&&a.sourceTexture.getHeight()==1100,"full supplied sheet dimensions");
            check(a.idle.getKeyFrames().length==6&&a.walk.getKeyFrames().length==6,"idle/walk counts");
            check(a.attack.getKeyFrames().length==10&&a.summon.getKeyFrames().length==11,"cast counts");
            check(a.hurt.getKeyFrames().length==4&&a.death.getKeyFrames().length==9,"hurt/death counts");
            check(a.summonEffect.getKeyFrames().length==7,"revival circle count");
            check(a.idle.getKeyFrames()[0].getRegionWidth()==100&&!a.sourceFacesLeft,"100px native right-facing cells");
            Engine engine=new Engine();engine.addEntity(enemy);engine.addSystem(new EnemyAnimationSystem());
            EnemyAIComponent ai=enemy.getComponent(EnemyAIComponent.class);
            NecromancerComponent n=enemy.getComponent(NecromancerComponent.class);
            engine.update(0.1f);check(a.state==AnimationComponent.State.IDLE,"idle");
            enemy.getComponent(VelocityComponent.class).vx=1f;
            engine.update(0.1f);check(a.state==AnimationComponent.State.WALK,"walk follows movement");
            n.shotVisualRemaining=0.6f;engine.update(0.01f);
            check(a.state==AnimationComponent.State.ATTACK&&Math.abs(a.stateTime-0.4f)<0.001f,"shot uses release pose");
            n.channeling=true;n.channelTimeRemaining=1f;engine.update(0.1f);
            check(a.state==AnimationComponent.State.SUMMON&&a.stateTime==1f,"revival synchronized to ritual");
            ai.state=EnemyAIComponent.State.STUNNED;engine.update(0.1f);
            check(a.state==AnimationComponent.State.HURT,"hurt overrides cast");
            ai.state=EnemyAIComponent.State.DEAD;engine.update(0.1f);
            check(a.state==AnimationComponent.State.DEAD,"death overrides all");
            engine.update(5f);
            check(a.getCurrentAnimation().getKeyFrame(a.stateTime)==a.death.getKeyFrames()[8],"death clamps without looping");
        } finally { a.sourceTexture.dispose();world.dispose(); }
        System.out.println("Necromancer visual regressions passed: "+checks+" assertions");
    }
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
}
