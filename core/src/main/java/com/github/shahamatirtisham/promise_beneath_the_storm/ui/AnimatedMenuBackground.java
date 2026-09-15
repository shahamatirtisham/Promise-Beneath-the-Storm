package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

/** Streams the 300 WebP menu frames through one texture at 20 frames per second. */
public final class AnimatedMenuBackground implements Disposable {
    public static final int FRAME_COUNT = 300;
    public static final double LOOP_SECONDS = 15.0;
    private static volatile boolean nativeDecoderAvailable = true;
    private final Texture texture;
    private final Image image;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "menu-background-decoder");
        thread.setDaemon(true);
        return thread;
    });
    private Future<BufferedImage> pending;
    private int pendingFrame;
    private double elapsed;
    private double pendingDue;
    private boolean disposed;

    public AnimatedMenuBackground() {
        Pixmap first = toPixmap(decodeFrame(frameFile(0)));
        try {
            texture = new Texture(first);
        } finally {
            first.dispose();
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        image = new Image(texture);
        schedule(1, LOOP_SECONDS / FRAME_COUNT);
    }

    public Image getImage() {
        return image;
    }

    public static int frameIndex(double seconds) {
        return (int) ((seconds % LOOP_SECONDS) * FRAME_COUNT / LOOP_SECONDS);
    }

    public void update(float delta) {
        if (disposed) return;
        elapsed += Math.max(0f, delta);
        int desiredFrame = frameIndex(elapsed);
        if (elapsed < pendingDue || !pending.isDone()) return;
        BufferedImage decoded;
        try {
            decoded = pending.get();
        } catch (Exception exception) {
            throw new GdxRuntimeException("Cannot decode menu background frame "
                + (pendingFrame + 1), exception);
        }
        Pixmap pixels = toPixmap(decoded);
        try {
            texture.draw(pixels, 0, 0);
        } finally {
            pixels.dispose();
        }
        // If decoding fell behind, catch up to the clock rather than extending the loop.
        double nextTick = Math.floor(elapsed * FRAME_COUNT / LOOP_SECONDS) + 1;
        schedule((desiredFrame + 1) % FRAME_COUNT, nextTick * LOOP_SECONDS / FRAME_COUNT);
    }

    private void schedule(int index, double due) {
        pendingFrame = index;
        pendingDue = due;
        FileHandle file = frameFile(index);
        pending = decoder.submit(() -> decodeFrame(file));
    }

    private static FileHandle frameFile(int index) {
        return Gdx.files.internal(String.format(Locale.ROOT,
            "backgrounds/main-menu-bg/frame_%04d.webp", index + 1));
    }

    public static BufferedImage decodeFrame(FileHandle file) {
        if (nativeDecoderAvailable) {
            try {
                return decodeFrame(file, true);
            } catch (LinkageError unsupportedPlatform) {
                // The bundled native decoder supports desktop x86 platforms.
                nativeDecoderAvailable = false;
            }
        }
        return decodeFrame(file, false);
    }

    private static BufferedImage decodeFrame(FileHandle file, boolean useNative) {
        ImageReader reader = null;
        try (InputStream input = file.read();
             MemoryCacheImageInputStream stream = new MemoryCacheImageInputStream(input)) {
            reader = useNative
                ? new com.luciad.imageio.webp.WebPImageReaderSpi().createReaderInstance()
                : new WebPImageReaderSpi().createReaderInstance();
            reader.setInput(stream);
            return reader.read(0);
        } catch (IOException exception) {
            throw new GdxRuntimeException("Cannot read menu background: " + file.path(), exception);
        } finally {
            if (reader != null) reader.dispose();
        }
    }

    private static Pixmap toPixmap(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] colors = image.getRGB(0, 0, width, height, null, 0, width);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        ByteBuffer pixels = pixmap.getPixels();
        for (int color : colors) {
            pixels.put((byte) (color >>> 16));
            pixels.put((byte) (color >>> 8));
            pixels.put((byte) color);
            pixels.put((byte) (color >>> 24));
        }
        pixels.flip();
        return pixmap;
    }

    @Override public void dispose() {
        if (disposed) return;
        disposed = true;
        pending.cancel(true);
        decoder.shutdownNow();
        texture.dispose();
    }
}
