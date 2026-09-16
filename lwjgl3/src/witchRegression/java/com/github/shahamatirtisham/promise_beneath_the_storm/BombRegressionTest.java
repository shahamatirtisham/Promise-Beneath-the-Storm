package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.dungeon.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.BombFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.utils.WorldUtils;
import java.lang.reflect.*;
import java.util.EnumMap;

/** Native wall geometry, real PNG decoding and production flight/damage without a window. */
public final class BombRegressionTest {
    private static int checks;
    private static boolean pressed;
    private static int pressedKey = Input.Keys.F;
    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load(); Box2D.init();
        Method graphics = ShieldGuardRegressionTest.class.getDeclaredMethod("initializeHeadlessGraphics");
        graphics.setAccessible(true); graphics.invoke(null);
        Gdx.input = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(), new Class<?>[]{Input.class},
            (p,m,a) -> m.getReturnType() == boolean.class ? pressed && m.getName().equals("isKeyJustPressed") && (Integer)a[0] == pressedKey : 0);
        RoomDefinition room = new RoomDefinition("test", "COMBAT", 20f, 20f, new Vector2(10f,10f),
            new Array<>(), new Array<>(), null, null, null, new EnumMap<>(GridDirection.class),
            new EnumMap<>(GridDirection.class), new Array<>(), new Array<>(), new Array<>(),
            new Array<>(), new Array<>(), new Array<>(), new Array<>(), null);
        World world = new World(new Vector2(),true);
        BombFactory resources = new BombFactory();
        try {
            check(resources.flight.getKeyFrames().length == 8, "eight bomb frames");
            for(int i=0;i<8;i++) {
                check(resources.flight.getKeyFrames()[i].getRegionX()==i*216, "stride");
                check(resources.flight.getKeyFrames()[i].getRegionWidth()==(i==7?215:216), "safe width");
            }
            check(resources.explosion.getKeyFrames().length==6, "visible explosion frames");
            check(resources.explosion.getKeyFrames()[0].getRegionX()==0, "visible bomb first frame");
            check(resources.explosion.getKeyFrames()[5].getRegionX()==240, "last visible bomb frame; blank trailing frame skipped");
            for(com.badlogic.gdx.graphics.g2d.TextureRegion frame:resources.explosion.getKeyFrames())
                check(frame.getRegionWidth()==48&&frame.getRegionHeight()==48,"square bomb explosion cells");
            check(Math.abs(resources.explosion.getAnimationDuration()-0.42f)<0.001f,"bomb visual cleanup duration");
            check(Math.abs(ExplosiveBarrelComponent.EXPLOSION_VISUAL_DURATION-0.84f)<0.001f,"barrel visual cleanup duration");
            WorldUtils.createWitchRoomBounds(world,20f,20f);
            for(float[] direction:new float[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1}}) {
                Vector2 target=BombSystem.landingPoint(world,room,10,10,10+30*direction[0],10+30*direction[1]);
                check(target.x>=0.12f&&target.x<=19.88f&&target.y>=0.12f&&target.y<=19.88f,"all boundaries/corners");
            }
            Body wall=WorldUtils.createStaticRectangle(world,new Rectangle(12,8,0.02f,4));
            Vector2 stopped=BombSystem.landingPoint(world,room,10,10,15,10);
            check(stopped.x<11.88f,"thin internal wall with clearance");
            world.destroyBody(wall);
            Engine engine=new Engine();
            Entity player=new Entity().add(new PlayerComponent()).add(new PositionComponent(10,10))
                .add(new FacingComponent()).add(new HealthComponent(100)).add(new TeamComponent(TeamComponent.Team.PLAYER));
            engine.addEntity(player);
            Entity enemy=enemy(15,10,TeamComponent.Team.ENEMY); engine.addEntity(enemy);
            Entity immune=enemy(15,10,TeamComponent.Team.ENEMY);
            immune.getComponent(InvulnerabilityComponent.class).timeRemaining=1;engine.addEntity(immune);
            Entity ally=enemy(15,10,TeamComponent.Team.PLAYER);engine.addEntity(ally);
            Entity distant=enemy(18,10,TeamComponent.Team.ENEMY);engine.addEntity(distant);
            final int[] potHits={0};
            BreakablePotSystem pots=new BreakablePotSystem(world,player,null,new Array<>(),p->{}) {
                @Override public boolean hit(Entity entity) { potHits[0]++; return true; }
            };
            Entity pot=new Entity().add(new BreakablePotComponent()).add(new PositionComponent(15,10));
            engine.addEntity(pot);
            BombSystem bombs=new BombSystem(player,world,()->room,resources,pots);engine.addSystem(bombs);
            check(bombs.throwBomb()==null,"no bombs initially");
            pressedKey=Input.Keys.MINUS;pressed=true;engine.update(0.01f);pressed=false;
            check(player.getComponent(PlayerComponent.class).bombCharges==5,"minus grants five");
            engine.update(0.01f);
            check(player.getComponent(PlayerComponent.class).bombCharges==5,"grant requires press");
            pressedKey=Input.Keys.F;
            Entity bomb=bombs.throwBomb();
            check(player.getComponent(PlayerComponent.class).bombCharges==4,"throw consumes one");
            engine.update(0.4f);
            check(Math.abs(bomb.getComponent(BombComponent.class).height-1.5f)<0.001f,"arc peak");
            check(Math.abs(bomb.getComponent(PositionComponent.class).x-12.5f)<0.001f,"ground midpoint");
            engine.update(0.4f);
            check(bomb.getComponent(BombComponent.class).exploded,"landed");
            check(enemy.getComponent(HealthComponent.class).current==65,"blast damage");
            check(immune.getComponent(HealthComponent.class).current==100,"immunity");
            check(ally.getComponent(HealthComponent.class).current==100,"team filtering");
            check(player.getComponent(HealthComponent.class).current==100,"no self damage");
            check(distant.getComponent(HealthComponent.class).current==100,"radius");
            check(potHits[0]==1,"pot delegates once");
            engine.update(0.3f);engine.update(0.3f);
            check(enemy.getComponent(HealthComponent.class).current==65&&potHits[0]==1,"no repeat blast");
            engine.update(0.3f);check(engine.getEntitiesFor(Family.all(BombComponent.class).get()).size()==0,"effect cleanup");
            pressed=true;engine.update(0.01f);pressed=false;engine.update(0.01f);
            check(engine.getEntitiesFor(Family.all(BombComponent.class).get()).size()==1,"one press one bomb");
            pressed=true;engine.update(0.01f);pressed=false;
            check(engine.getEntitiesFor(Family.all(BombComponent.class).get()).size()==1,"cooldown");
            check(player.getComponent(PlayerComponent.class).bombCharges==3,"cooldown consumes no extra ammo");
            bombs.clear();check(engine.getEntitiesFor(Family.all(BombComponent.class).get()).size()==0,"room cleanup");
            while(player.getComponent(PlayerComponent.class).bombCharges>0) check(bombs.throwBomb()!=null,"remaining inventory usable");
            check(bombs.throwBomb()==null,"empty inventory prevents throw");
            pressedKey=Input.Keys.NUMPAD_SUBTRACT;pressed=true;engine.update(0.01f);pressed=false;
            check(player.getComponent(PlayerComponent.class).bombCharges==5,"numpad minus grants five");
            bombs.clear();
            player.add(new AttackComponent()).add(new InvulnerabilityComponent());
            Array<Entity> barrels=new Array<>();
            for(float x:new float[]{15.5f,17.5f,19.9f}) {
                Entity barrel=new Entity().add(new ExplosiveBarrelComponent()).add(new PositionComponent(x,10));
                barrels.add(barrel);engine.addEntity(barrel);
            }
            int[] explosions={0};
            engine.addSystem(new ExplosiveBarrelSystem(player,new Array<>(),barrels,b->explosions[0]++));
            bombs.throwBomb();engine.update(0.8f);
            ExplosiveBarrelComponent first=barrels.get(0).getComponent(ExplosiveBarrelComponent.class);
            ExplosiveBarrelComponent chained=barrels.get(1).getComponent(ExplosiveBarrelComponent.class);
            ExplosiveBarrelComponent outside=barrels.get(2).getComponent(ExplosiveBarrelComponent.class);
            check(first.destroyed&&!first.explosionApplied,"bomb primes existing barrel fuse");
            check(Math.abs(first.fuseTimeRemaining-0.45f)<0.001f,"existing fuse duration preserved");
            check(!chained.destroyed&&outside.health==30f,"bomb blast radius limits barrel triggering");
            engine.update(0.46f);
            check(first.explosionApplied&&explosions[0]==1,"bomb-triggered barrel explodes once");
            check(chained.destroyed&&!chained.explosionApplied,"barrel chain reaction preserved");
            engine.update(0.46f);
            check(chained.explosionApplied&&explosions[0]==2,"chained barrel explodes");
            check(!outside.destroyed&&outside.health==30f,"barrel outside chain radius unaffected");
            engine.update(0.46f);
            check(explosions[0]==2,"blast does not restart destroyed barrel fuses");
            bombs.clear();
            player.add(new PlayerRangedComponent()).add(new RunInventoryComponent());
            Entity merchant=com.github.shahamatirtisham.promise_beneath_the_storm.entities.MerchantFactory
                .createRelicMerchant(new Vector2(10,10),1,123L);
            MerchantComponent stock=merchant.getComponent(MerchantComponent.class);
            check(stock.offerTypes.length==6&&stock.offerTypes[5]==MerchantOfferType.BOMB,"bomb appended to merchant stock");
            check(stock.costs[5]==20,"bomb price is twenty coins");
            engine.addEntity(merchant);engine.addSystem(new MerchantSystem(player));
            RunInventoryComponent wallet=player.getComponent(RunInventoryComponent.class);
            int ammo=player.getComponent(PlayerComponent.class).bombCharges;
            wallet.devilCoins=19;pressedKey=Input.Keys.NUM_6;pressed=true;engine.update(0.01f);pressed=false;
            check(wallet.devilCoins==19&&player.getComponent(PlayerComponent.class).bombCharges==ammo,"insufficient coins reject purchase");
            wallet.devilCoins=40;pressed=true;engine.update(0.01f);pressed=false;
            check(wallet.devilCoins==20&&player.getComponent(PlayerComponent.class).bombCharges==ammo+1,"purchase costs twenty and adds one bomb");
            engine.update(0.01f);
            check(wallet.devilCoins==20,"purchase requires new key press");
            pressed=true;engine.update(0.01f);pressed=false;
            check(wallet.devilCoins==0&&player.getComponent(PlayerComponent.class).bombCharges==ammo+2&&!stock.isPurchased(5),"bomb stock repeatable");
            wallet.devilCoins=20;merchant.getComponent(PositionComponent.class).x=18;
            pressed=true;engine.update(0.01f);pressed=false;
            check(wallet.devilCoins==20,"purchase requires merchant proximity");
            merchant.getComponent(PositionComponent.class).x=10;pressedKey=Input.Keys.NUMPAD_6;
            pressed=true;engine.update(0.01f);pressed=false;
            check(wallet.devilCoins==0&&player.getComponent(PlayerComponent.class).bombCharges==ammo+3,"numpad six buys bomb");
        } finally {resources.dispose();resources.dispose();world.dispose();}
        System.out.println("Bomb regressions passed: "+checks+" assertions");
    }
    private static Entity enemy(float x,float y,TeamComponent.Team team) {
        return new Entity().add(new EnemyComponent()).add(new EnemyAIComponent())
            .add(new PositionComponent(x,y)).add(new HealthComponent(100))
            .add(new InvulnerabilityComponent()).add(new TeamComponent(team));
    }
    private static void check(boolean value,String message) {checks++;if(!value)throw new AssertionError(message);}
}
