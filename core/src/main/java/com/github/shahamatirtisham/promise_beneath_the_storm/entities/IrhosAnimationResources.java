package com.github.shahamatirtisham.promise_beneath_the_storm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.BossComponent.Phase;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.IrhosAnimationComponent.Action;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.IrhosAnimationComponent.Direction;
import java.util.EnumMap;

/** One cache per GameScreen, shared across boss retries and disposed with the screen. */
public final class IrhosAnimationResources implements Disposable {
    private static final String ROOT = "characters/irhos/";
    private static final String[] FORMS = {"IronFist", "BurningGauntlets", "DevilsCrown", "IrhosRevealed"};
    private final EnumMap<Phase, EnumMap<Action, Clip>> clips = new EnumMap<>(Phase.class);
    private Texture revealedWizardEffectTexture;
    private TextureRegion[] revealedWizardEffectFrames;

    public static final class Clip {
        public final TextureRegion[][] rows;
        public final boolean loop;
        private final Texture texture;

        private Clip(Texture texture, int[] counts, boolean loop) {
            this.texture = texture;
            this.loop = loop;
            TextureRegion[][] cells = TextureRegion.split(texture, 100, 100);
            rows = new TextureRegion[4][];
            for (int row = 0; row < 4; row++) {
                if (counts[row] < 1 || counts[row] > cells[row].length)
                    throw new IllegalArgumentException("Invalid Irhos row count");
                rows[row] = new TextureRegion[counts[row]];
                System.arraycopy(cells[row], 0, rows[row], 0, counts[row]);
            }
        }

        /** Finite clips clamp at the last real pose, including the short padded rows. */
        public TextureRegion atProgress(Direction direction, float progress, int firstFrame) {
            TextureRegion[] frames = rows[direction.ordinal()];
            int index = firstFrame + (int) (Math.max(0f, progress) * (frames.length - firstFrame));
            return frames[Math.min(frames.length - 1, index)];
        }

        public TextureRegion atTime(Direction direction, float time, float frameDuration) {
            TextureRegion[] frames = rows[direction.ordinal()];
            int index = (int) (Math.max(0f, time) / frameDuration);
            return frames[loop ? index % frames.length : Math.min(index, frames.length - 1)];
        }
    }

    public void load() {
        if (!clips.isEmpty()) return;
        JsonValue manifest = new JsonReader().parse(Gdx.files.internal(ROOT + "manifest.json"));
        int[] cell = manifest.get("frameSize").asIntArray();
        String[] directions = manifest.get("directionOrder").asStringArray();
        if (cell.length != 2 || cell[0] != 100 || cell[1] != 100
            || !java.util.Arrays.equals(directions, new String[] {"Down", "Left", "Right", "Up"}))
            throw new IllegalArgumentException("Unexpected Irhos cell size/direction order");
        try {
            for (Phase phase : Phase.values()) {
                String form = FORMS[phase.ordinal()];
                EnumMap<Action, Clip> actions = new EnumMap<>(Action.class);
                clips.put(phase, actions);
                for (JsonValue sheet : manifest.get("combinedSheets").get(form)) {
                    String name = sheet.getString("name");
                    // There is no non-lethal ending. All other 36 sheets are cached.
                    if (name.equals("DefeatedAlive")) continue;
                    Action action = Action.valueOf(name);
                    String file = sheet.getString("file");
                    if (!file.startsWith("sheets/") || !file.endsWith("_4dir.png"))
                        throw new IllegalArgumentException("Irhos runtime asset must be a combined sheet: " + file);
                    boolean loop = false;
                    for (JsonValue metadata : manifest.get("forms").get(form).get("Down")) {
                        if (metadata.getString("name").equals(name)) loop = metadata.getBoolean("loop");
                    }
                    if (loop != (action == Action.Idle || action == Action.Walk))
                        throw new IllegalArgumentException("Unexpected Irhos looping metadata: " + file);
                    int[] size = sheet.get("size").asIntArray();
                    int[] counts = sheet.get("rowCounts").asIntArray();
                    Texture texture = new Texture(Gdx.files.internal(ROOT + file));
                    try {
                        if (counts.length != 4 || texture.getWidth() != size[0]
                            || texture.getHeight() != size[1] || size[1] != 400 || size[0] % 100 != 0)
                            throw new IllegalArgumentException("Unexpected Irhos sheet dimensions: " + file);
                        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                        actions.put(action, new Clip(texture, counts, loop));
                    } catch (RuntimeException failure) {
                        texture.dispose();
                        throw failure;
                    }
                }
            }
        } catch (RuntimeException failure) {
            dispose();
            throw failure;
        }
    }

    public Clip get(Phase form, Action action) {
        load();
        Clip clip = clips.get(form).get(action);
        if (clip == null) throw new IllegalArgumentException("Missing Irhos clip: " + form + "/" + action);
        return clip;
    }

    public TextureRegion getRevealedWizardEffectFrame(int frame) {
        if (revealedWizardEffectFrames == null) {
            String path = ROOT + "irhos_revealed_fx/Wizard_Attack02_Effect.png";
            revealedWizardEffectTexture = new Texture(Gdx.files.internal(path));
            if (revealedWizardEffectTexture.getWidth() != 700
                || revealedWizardEffectTexture.getHeight() != 100) {
                revealedWizardEffectTexture.dispose();
                revealedWizardEffectTexture = null;
                throw new IllegalArgumentException("Unexpected Irhos revealed effect dimensions: " + path);
            }
            revealedWizardEffectTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            revealedWizardEffectFrames = TextureRegion.split(revealedWizardEffectTexture, 100, 100)[0];
        }
        return revealedWizardEffectFrames[frame];
    }

    @Override public void dispose() {
        for (EnumMap<Action, Clip> actions : clips.values())
            for (Clip clip : actions.values()) clip.texture.dispose();
        clips.clear();
        if (revealedWizardEffectTexture != null) revealedWizardEffectTexture.dispose();
        revealedWizardEffectTexture = null;
        revealedWizardEffectFrames = null;
    }
}
