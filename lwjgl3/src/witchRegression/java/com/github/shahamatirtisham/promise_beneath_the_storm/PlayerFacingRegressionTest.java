package com.github.shahamatirtisham.promise_beneath_the_storm;

import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.FacingComponent.Direction;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent;
import com.github.shahamatirtisham.promise_beneath_the_storm.components.PlayerAnimationComponent.State;
import com.github.shahamatirtisham.promise_beneath_the_storm.systems.PlayerAnimationSystem;

public final class PlayerFacingRegressionTest {
    public static void main(String[] args) {
        FacingComponent facing = new FacingComponent();
        PlayerAnimationComponent animation = new PlayerAnimationComponent();
        float[][] vectors = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        Direction[] directions = {Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN};
        for (Direction walking : directions) for (int i = 0; i < directions.length; i++) {
            facing.direction = walking;
            facing.x = vectors[i][0]; facing.y = vectors[i][1];
            for (State state : new State[] {State.ATTACK, State.BLOCK, State.IDLE, State.WALK}) {
                animation.state = state;
                Direction expected = state == State.ATTACK || state == State.BLOCK ? directions[i] : walking;
                if (PlayerAnimationSystem.renderDirection(animation, facing) != expected)
                    throw new AssertionError(state + " wrong direction");
                if (facing.direction != walking) throw new AssertionError("movement facing mutated");
            }
            animation.state = State.PARRY;
            animation.parryDirection = directions[i];
            facing.x = -vectors[i][0]; facing.y = -vectors[i][1];
            if (PlayerAnimationSystem.renderDirection(animation, facing) != directions[i])
                throw new AssertionError("parry must retain its captured direction");
        }
        System.out.println("Player facing passed: all movement/aim combinations, attack/block, locked parry");
    }
}
