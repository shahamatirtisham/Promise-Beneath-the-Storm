package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import java.lang.reflect.*;

/** Production interaction timing, one-time reward delivery and restored visual endpoints. */
public final class LeverChestRegressionTest {
    private static boolean pressed;
    private static int checks;
    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Method setup=ShieldGuardRegressionTest.class.getDeclaredMethod("initializeHeadlessGraphics");
        setup.setAccessible(true); setup.invoke(null);
        Preferences preferences=(Preferences)Proxy.newProxyInstance(Preferences.class.getClassLoader(),
            new Class<?>[]{Preferences.class},(p,m,a)->a!=null&&a.length==2?a[1]:null);
        Application originalApp=Gdx.app;
        Gdx.app=(Application)Proxy.newProxyInstance(Application.class.getClassLoader(),
            new Class<?>[]{Application.class},(p,m,a)->m.getName().equals("getPreferences")?preferences:m.invoke(originalApp,a));
        Gdx.input=(Input)Proxy.newProxyInstance(Input.class.getClassLoader(),new Class<?>[]{Input.class},
            (p,m,a)->m.getReturnType()==boolean.class ? pressed && m.getName().startsWith("is") : 0);
        Engine engine=new Engine();
        Entity player=new Entity().add(new PlayerComponent()).add(new PositionComponent(0,0));
        Entity lever=LeverFactory.create(new Vector2(0,0));
        Entity chest=ChestFactory.create(new Vector2(0,0),CollectableComponent.Type.DEVIL_COINS,7,null);
        Array<Entity> pickups=new Array<>();
        engine.addEntity(player);engine.addEntity(lever);engine.addEntity(chest);
        engine.addSystem(new LeverSystem(player));engine.addSystem(new ChestSystem(engine,player,pickups));
        LeverComponent l=lever.getComponent(LeverComponent.class);
        ChestComponent c=chest.getComponent(ChestComponent.class);
        ChestAnimationComponent animation=chest.getComponent(ChestAnimationComponent.class);
        pressed=true;player.getComponent(PositionComponent.class).x=3;
        engine.update(0.01f); check(!l.pulling,"out of range rejected");
        player.getComponent(PositionComponent.class).x=0;player.getComponent(PlayerComponent.class).dead=true;
        engine.update(0.01f);check(!l.pulling,"dead player rejected");
        player.getComponent(PlayerComponent.class).dead=false;
        engine.update(0.01f);
        check(l.pulling&&!l.activated&&l.frameIndex()==0,"pull starts at first frame without unlocking");
        check(!c.opening&&pickups.size==0,"locked chest cannot open");
        engine.update(0.13f);check(l.frameIndex()==1&&!l.activated,"second lever frame");
        engine.update(0.12f);check(l.frameIndex()==2&&!l.activated,"third lever frame");
        engine.update(0.12f);check(l.frameIndex()==3&&!l.activated,"fourth lever frame");
        engine.update(0.12f);check(l.frameIndex()==4&&!l.activated,"last frame before completion");
        engine.update(0.12f);check(l.activated&&!l.pulling,"pull completes once");
        c.unlocked=l.activated;c.revealed=c.unlocked;
        engine.update(0.01f);check(c.opening&&!c.opened&&pickups.size==0,"opening starts with no early loot");
        pressed=false;
        engine.update(animation.normal.getAnimationDuration()/2);
        check(c.opening&&pickups.size==0,"mid-animation loot withheld");
        float pausedTime=animation.stateTime;
        engine.getSystem(ChestSystem.class).setProcessing(false);
        engine.update(2f);
        check(animation.stateTime==pausedTime&&pickups.size==0,"paused system does not advance or release loot");
        engine.getSystem(ChestSystem.class).setProcessing(true);
        pressed=true;engine.update(animation.normal.getAnimationDuration());
        check(c.opened&&!c.opening&&pickups.size==1,"one reward at animation completion");
        engine.update(5f);engine.update(5f);
        check(pickups.size==1&&l.frameIndex()==4,"repeated interactions do not duplicate reward or reset lever");
        Entity restored=LeverFactory.create(new Vector2());
        restored.getComponent(LeverComponent.class).restoreActivated();
        check(restored.getComponent(LeverComponent.class).frameIndex()==4,"restored lever holds pulled pose");
        com.badlogic.gdx.graphics.Pixmap sheet = new com.badlogic.gdx.graphics.Pixmap(Gdx.files.internal("maps/doors_lever_chest_animation.png"));
        com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(sheet);
        com.badlogic.gdx.graphics.g2d.TextureRegion[] frames = LeverFactory.createFrames(texture);
        check(frames.length==5,"five lever poses");
        for (int i=0;i<frames.length;i++) {
            com.badlogic.gdx.graphics.g2d.TextureRegion frame=frames[i];
            check(frame.getRegionWidth()==16&&frame.getRegionHeight()==16&&frame.getRegionX()==i*16,
                "one lever per frame, no adjacent pose");
            int visible=0;
            for(int y=0;y<16;y++) for(int x=0;x<16;x++)
                if((sheet.getPixel(frame.getRegionX()+x,frame.getRegionY()+y)&255)>0) visible++;
            check(visible>0,"lever frame remains visible: "+i);
        }
        texture.dispose();sheet.dispose();
        check(animation.normal.getKeyFrame(animation.stateTime,false)==animation.normal.getKeyFrames()[animation.normal.getKeyFrames().length-1],"opened chest holds final frame");
        animation.hidden.getKeyFrames()[0].getTexture().dispose();
        animation.normal.getKeyFrames()[0].getTexture().dispose();
        System.out.println("Lever/chest regressions passed: "+checks+" assertions");
    }
    private static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
}
