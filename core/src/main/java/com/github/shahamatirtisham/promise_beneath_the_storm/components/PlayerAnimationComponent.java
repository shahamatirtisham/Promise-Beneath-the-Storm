package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;
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
        HURT,
        DEAD
    }

    public State state = State.IDLE;
}
