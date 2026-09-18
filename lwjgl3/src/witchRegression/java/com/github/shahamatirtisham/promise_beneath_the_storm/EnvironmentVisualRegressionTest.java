package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.github.shahamatirtisham.promise_beneath_the_storm.ui.DarknessOverlay;

/** Real GL coverage: a bright world cannot leak through the outer darkness. */
public final class EnvironmentVisualRegressionTest extends ApplicationAdapter {
    private static Throwable failure;
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setInitialVisible(false);
        config.setWindowedMode(400, 400);
        config.disableAudio(true);
        new Lwjgl3Application(new EnvironmentVisualRegressionTest(), config);
        if (failure != null) throw new AssertionError("Environment visual regression", failure);
        System.out.println("Environment visuals passed: circular fade, opaque exterior, fire sheets and TMX loading");
    }
    @Override public void create() {
        ShapeRenderer shapes = new ShapeRenderer();
        try {
            Gdx.gl.glViewport(0, 0, 400, 400);
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.setProjectionMatrix(new Matrix4().setToOrtho2D(-5, -5, 10, 10));
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            new DarknessOverlay().draw(shapes, 0, 0, 20);
            shapes.end();
            Pixmap pixels = Pixmap.createFromFrameBuffer(0, 0, 400, 400);
            try {
                check(red(pixels, 200, 200) > 250, "center is clear");
                int fade = red(pixels, 318, 200);
                check(fade > 80 && fade < 180, "soft midpoint");
                check(Math.abs(fade - red(pixels, 200, 318)) < 5, "circular symmetry");
                check(red(pixels, 360, 200) < 5, "outer world completely hidden");
                check(red(pixels, 304, 304) < 5, "old square corners now hidden");
            } finally { pixels.dispose(); }
            for (int i = 1; i <= 2; i++) {
                String path = "maps/fire_regioen_small_" + i + ".png";
                check(java.util.Arrays.equals(Gdx.files.local(path).readBytes(),
                    Gdx.files.classpath(path).readBytes()), "classpath fire matches source assets (no stale duplicate)");
                Pixmap fire = new Pixmap(Gdx.files.internal("maps/fire_regioen_small_" + i + ".png"));
                try {
                    int size = i == 1 ? 128 : 80;
                    check(fire.getWidth() == size*3 && fire.getHeight() == size*2, "fire sheet dimensions");
                    for (int frame = 0; frame < 6; frame++) {
                        boolean visible = false;
                        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
                            visible |= (fire.getPixel(frame%3*size+x, frame/3*size+y) & 255) > 0;
                        check(visible, "nonempty fire animation frame");
                    }
                } finally { fire.dispose(); }
            }
            TiledMap map = new TmxMapLoader().load("maps/room_fire.tmx");
            try { check(map.getLayers().get("fire tile") != null, "fire map loads"); }
            finally { map.dispose(); }
        } catch (Throwable ex) { failure = ex; }
        finally { shapes.dispose(); Gdx.app.exit(); }
    }
    private static int red(Pixmap image, int x, int y) { return image.getPixel(x, y) >>> 24; }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
