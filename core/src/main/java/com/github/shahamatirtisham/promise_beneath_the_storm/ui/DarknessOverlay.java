package com.github.shahamatirtisham.promise_beneath_the_storm.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** World-space circular visibility mask, drawn after every world/debug layer. */
public final class DarknessOverlay {
    public static final float CLEAR_RADIUS = 2.4f;
    public static final float OUTER_RADIUS = 3.5f;
    private static final int SEGMENTS = 96;
    private static final int BANDS = 16;
    private final Color inner = new Color();
    private final Color outer = new Color();

    public static float opacity(float distance) {
        float t = Math.max(0f, Math.min(1f,
            (distance - CLEAR_RADIUS) / (OUTER_RADIUS - CLEAR_RADIUS)));
        return t * t * (3f - 2f * t);
    }

    public void draw(ShapeRenderer renderer, float x, float y, float farRadius) {
        for (int band = 0; band <= BANDS; band++) {
            float r0 = CLEAR_RADIUS + (OUTER_RADIUS - CLEAR_RADIUS) * band / BANDS;
            float r1 = band == BANDS ? Math.max(farRadius, OUTER_RADIUS + 1f)
                : CLEAR_RADIUS + (OUTER_RADIUS - CLEAR_RADIUS) * (band + 1) / BANDS;
            inner.set(0.005f, 0.005f, 0.018f, opacity(r0));
            outer.set(0.005f, 0.005f, 0.018f, opacity(r1));
            for (int i = 0; i < SEGMENTS; i++) {
                double a = Math.PI * 2 * i / SEGMENTS;
                double b = Math.PI * 2 * (i + 1) / SEGMENTS;
                float ax = (float) Math.cos(a), ay = (float) Math.sin(a);
                float bx = (float) Math.cos(b), by = (float) Math.sin(b);
                renderer.triangle(x + ax*r0, y + ay*r0, x + ax*r1, y + ay*r1,
                    x + bx*r1, y + by*r1, inner, outer, outer);
                renderer.triangle(x + ax*r0, y + ay*r0, x + bx*r1, y + by*r1,
                    x + bx*r0, y + by*r0, inner, outer, inner);
            }
        }
    }
}
