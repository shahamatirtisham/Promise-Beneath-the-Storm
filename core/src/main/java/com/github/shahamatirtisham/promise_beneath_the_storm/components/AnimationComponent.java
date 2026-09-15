package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;

public class AnimationComponent implements Component {

    public Animation<TextureRegion> idle;
    public Animation<TextureRegion> walk;
    public Animation<TextureRegion> attack;
    public Animation<TextureRegion>[] attackVariants;
    public Animation<TextureRegion> block;
    public Animation<TextureRegion> charge;
    public Animation<TextureRegion> hurt;
    public Animation<TextureRegion> death;
    public Texture sourceTexture;

    // Per-sheet presentation settings. Defaults preserve the Orc rendering.
    public float renderWidth = 4.2f;
    public float renderHeight = 4.2f;
    public float renderYOffset = 1.9f;
    public float hoverAmplitude;
    public float hoverFrequency = 6f;
    public boolean sourceFacesLeft;
    // Optional pixel-based anchoring for sheets with different frame sizes.
    public float renderPixelScale;
    public float bodyAnchorX;
    public float attackBodyAnchorX;
    public float chargeBodyAnchorX;
    public float previousHealth = Float.NaN;
    public float hurtTimeRemaining;

    public float stateTime = 0f;
    public boolean facingLeft = false;

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        CHARGE,
        BLOCK,
        HURT,
        DEAD
    }

    public State state = State.IDLE;

    public Animation<TextureRegion> getCurrentAnimation() {

        switch (state) {

            case WALK:
                return walk;

            case ATTACK:
                return attack;

            case CHARGE:
                return charge;

            case BLOCK:
                return block;

            case HURT:
                return hurt;

            case DEAD:
                return death;

            case IDLE:
            default:
                return idle;
        }
    }
}
