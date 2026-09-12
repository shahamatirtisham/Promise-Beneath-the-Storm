package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerAnimationComponent implements Component {

    // Idle
    public Animation<TextureRegion> idleDown;
    public Animation<TextureRegion> idleSide;
    public Animation<TextureRegion> idleUp;

    // Walk
    public Animation<TextureRegion> walkDown;
    public Animation<TextureRegion> walkSide;
    public Animation<TextureRegion> walkUp;

    // Attack
    public Animation<TextureRegion> attackDown;
    public Animation<TextureRegion> attackSide;
    public Animation<TextureRegion> attackUp;

    // Directional three-frame blocking animation.
    public Animation<TextureRegion> blockDown;
    public Animation<TextureRegion> blockLeft;
    public Animation<TextureRegion> blockRight;
    public Animation<TextureRegion> blockUp;
    public Texture blockTexture;

    // Directional four-frame perfect-parry animation.
    public Animation<TextureRegion> parryDown;
    public Animation<TextureRegion> parryLeft;
    public Animation<TextureRegion> parryRight;
    public Animation<TextureRegion> parryUp;
    public Texture parryTexture;
    public boolean parryAnimationRequested;
    public FacingComponent.Direction parryDirection =
        FacingComponent.Direction.DOWN;

    // Hurt
    public Animation<TextureRegion> hurtDown;
    public Animation<TextureRegion> hurtSide;
    public Animation<TextureRegion> hurtUp;

    // Death
    public Animation<TextureRegion> deathDown;
    public Animation<TextureRegion> deathSide;
    public Animation<TextureRegion> deathUp;
    public Animation<TextureRegion> dust;

    public float stateTime = 0f;
    public float dustStateTime = 0f;

    public boolean facingLeft = false;

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        BLOCK,
        PARRY,
        HURT,
        DEAD
    }

    public State state = State.IDLE;

    /** Captures the direction of a confirmed parry for visual playback. */
    public void requestParryAnimation(float facingX, float facingY) {
        if (Math.abs(facingX) > Math.abs(facingY)) {
            parryDirection = facingX < 0f
                ? FacingComponent.Direction.LEFT
                : FacingComponent.Direction.RIGHT;
        } else {
            parryDirection = facingY < 0f
                ? FacingComponent.Direction.DOWN
                : FacingComponent.Direction.UP;
        }
        parryAnimationRequested = true;
    }
}
