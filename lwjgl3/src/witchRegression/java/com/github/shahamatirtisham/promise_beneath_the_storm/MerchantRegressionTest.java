package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.*;
import com.github.shahamatirtisham.promise_beneath_the_storm.entities.MerchantFactory;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.MerchantSystem;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.GameHud;

/** Exercises real Scene2D shop listeners and the production purchase path. */
public final class MerchantRegressionTest extends ApplicationAdapter {
    private static int checks;
    private static Throwable failure;
    private GameHud hud;
    private Entity player, merchant;
    private MerchantSystem shop;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setInitialVisible(false);
        config.setWindowedMode(960, 540);
        config.disableAudio(true);
        new Lwjgl3Application(new MerchantRegressionTest(), config);
        if (failure != null) throw new AssertionError("Merchant regression failed", failure);
        System.out.println("Merchant regressions passed: " + checks + " assertions");
    }

    @Override public void create() {
        try { verify(); } catch (Throwable ex) { failure = ex; }
        finally { if (hud != null) hud.dispose(); Gdx.app.exit(); }
    }

    private void open() {
        hud.showMerchantMenu(merchant.getComponent(MerchantComponent.class),
            i -> shop.purchaseOffer(merchant, i), i -> shop.getUnavailableReason(merchant, i),
            shop::getInventorySummary);
        hud.getStage().act(0f);
    }

    private TextButton button(int index) {
        return hud.getStage().getRoot().findActor("merchant-offer-" + index);
    }

    private void buy(int index) {
        check(!button(index).isDisabled(), "offer enabled: " + index);
        button(index).setChecked(true); // Fires the real Scene2D ChangeListener.
    }

    private void verify() {
        player = new Entity().add(new PlayerComponent()).add(new PositionComponent(10, 10))
            .add(new PlayerRangedComponent()).add(new RunInventoryComponent())
            .add(new RelicInventoryComponent()).add(new HealthComponent(100))
            .add(new AttackComponent()).add(new DashComponent());
        merchant = MerchantFactory.createRelicMerchant(new Vector2(10, 10), 1, 123L);
        shop = new MerchantSystem(player);
        hud = new GameHud(() -> {}, () -> {});
        hud.getStage().getViewport().update(960, 540, true);
        RunInventoryComponent wallet = player.getComponent(RunInventoryComponent.class);
        PlayerRangedComponent knives = player.getComponent(PlayerRangedComponent.class);
        PlayerComponent state = player.getComponent(PlayerComponent.class);
        MerchantComponent stock = merchant.getComponent(MerchantComponent.class);
        wallet.devilCoins = 1000;
        open();
        hud.getStage().getViewport().apply();
        hud.getStage().draw();
        com.badlogic.gdx.graphics.Pixmap screenshot = com.badlogic.gdx.graphics.Pixmap.createFromFrameBuffer(0,0,960,540);
        com.badlogic.gdx.files.FileHandle report=Gdx.files.local("../lwjgl3/build/reports/merchant-shop.png");
        report.parent().mkdirs();
        com.badlogic.gdx.graphics.PixmapIO.writePNG(report, screenshot, -1, true);
        screenshot.dispose();
        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scroll=hud.getStage().getRoot().findActor("merchant-offers");
        scroll.setScrollPercentY(1f);
        scroll.updateVisualScroll();
        hud.getStage().draw();
        Vector2 bombLocation=button(5).localToStageCoordinates(new Vector2(0,0));
        Vector2 scrollLocation=scroll.localToStageCoordinates(new Vector2(0,0));
        check(bombLocation.y>=scrollLocation.y-1f
            && bombLocation.y+button(5).getHeight()<=scrollLocation.y+scroll.getHeight()+1f,
            "last offer accessible by scrolling at minimum window size");
        check(hud.getStage().getScrollFocus()==scroll,"wheel scroll targets merchant offers");
        check(hud.isPaused(), "shop pauses gameplay");
        check(button(0).isDisabled(), "full knife pouch disables purchase");
        shop.purchaseOffer(merchant, 0);
        check(wallet.devilCoins == 1000 && knives.charges == 1, "full pouch cannot spend");
        int before = wallet.devilCoins;
        buy(1);
        check(knives.maximumCharges == 2 && knives.charges == 1, "pouch adds capacity, not a knife");
        check(wallet.devilCoins == before-stock.costs[1], "pouch exact charge");
        check(button(1).isDisabled() && !button(0).isDisabled(), "pouch sold and knife enabled immediately");
        before=wallet.devilCoins;
        buy(0);
        check(knives.charges == 2 && wallet.devilCoins == before-stock.costs[0], "knife delivered and charged once");
        check(button(0).isDisabled() && !stock.isPurchased(0), "knife full but renewable");
        knives.charges--; open(); buy(0);
        check(knives.charges==2, "knife can be bought after consuming one");
        for (int index=2; index<=4; index++) {
            RelicType type=stock.relicOffers[index];
            float hp=player.getComponent(HealthComponent.class).maximum;
            float damage=player.getComponent(AttackComponent.class).damage;
            float cooldown=player.getComponent(DashComponent.class).cooldownDuration;
            int count=player.getComponent(RelicInventoryComponent.class).total();
            before=wallet.devilCoins; buy(index);
            check(wallet.devilCoins==before-stock.costs[index], "relic exact price");
            check(player.getComponent(RelicInventoryComponent.class).total()==count+1, "relic inventory increment");
            check(button(index).isDisabled(), "relic sold immediately");
            if(type==RelicType.IRON_HEART) check(player.getComponent(HealthComponent.class).maximum==hp+15, "health relic effect");
            if(type==RelicType.STORM_EDGE) check(player.getComponent(AttackComponent.class).damage==damage+3, "damage relic effect");
            if(type==RelicType.WINDSTEP_SIGIL) check(Math.abs(player.getComponent(DashComponent.class).cooldownDuration-Math.max(0.5f,cooldown-0.08f))<0.001f, "dash relic effect");
            before=wallet.devilCoins; shop.purchaseOffer(merchant,index);
            check(wallet.devilCoins==before && player.getComponent(RelicInventoryComponent.class).total()==count+1, "duplicate relic rejected");
        }
        wallet.devilCoins=40; open(); buy(5); buy(5);
        check(state.bombCharges==2 && wallet.devilCoins==0 && !stock.isPurchased(5), "two bombs, exact price, renewable stock");
        check(button(5).isDisabled(), "unaffordable bomb disabled immediately");
        check(shop.purchaseOffer(merchant,5).contains("more coins") && state.bombCharges==2, "insufficient funds return feedback without delivery");
        wallet.devilCoins=20; state.dead=true;
        shop.purchaseOffer(merchant,5);
        check(wallet.devilCoins==20 && state.bombCharges==2, "dead player cannot buy");
        state.dead=false;
        player.getComponent(PositionComponent.class).x=20;
        shop.purchaseOffer(merchant,5);
        check(wallet.devilCoins==20 && state.bombCharges==2, "out of range cannot buy");
        stock.interactionBounds=new Rectangle(19,9,2,2);
        open(); buy(5);
        check(state.bombCharges==3 && wallet.devilCoins==0, "Tiled interaction rectangle authorizes purchase");
        wallet.devilCoins=1000; player.getComponent(PositionComponent.class).x=10;
        int mask=stock.purchasedMask;
        merchant=MerchantFactory.createRelicMerchant(new Vector2(10,10),1,123L);
        merchant.getComponent(MerchantComponent.class).purchasedMask=mask;
        open();
        for(int i=1;i<=4;i++) check(button(i).isDisabled(), "restored merchant retains sold stock");
        merchant=MerchantFactory.createRelicMerchant(new Vector2(10,10),2,456L);
        knives.maximumCharges=PlayerRangedComponent.MAXIMUM_POUCH_CAPACITY;
        open(); check(button(1).isDisabled(), "max capacity disabled in new shop");
        before=wallet.devilCoins; shop.purchaseOffer(merchant,1);
        check(wallet.devilCoins==before && knives.maximumCharges==5, "maximum capacity cannot spend");
        check(shop.purchaseOffer(merchant,-1)!=null && shop.purchaseOffer(null,0)!=null, "invalid request safe");
        check(shop.getInventorySummary().contains("Bombs: 3"), "shop shows actual bomb inventory");
        hud.closePauseMenu();
        check(!hud.isPaused() && hud.consumeGameplayInputBlock(), "close resumes with click suppression");
    }

    private static void check(boolean condition,String description) {
        checks++;
        if(!condition) throw new AssertionError(description);
    }
}
