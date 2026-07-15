package com.github.shahamatirtisham.promise_beneath_the_storm.components;

import com.badlogic.ashley.core.Component;

/** Runtime state and tuning values for the basic melee-enemy state machine. */
public class EnemyAIComponent implements Component {
    public enum State {
        IDLE,
        CHASE,
        ATTACK,
        RECOVER,
        DEAD
    }

    public State state = State.IDLE;
    public float stateTimeRemaining;
    public float detectionRange = 3f;
    public float attackRange = 1.15f;
    public float movementSpeed = 2.2f;
    public float attackWindup = 0.35f;
    public float recoveryDuration = 0.55f;
    public float attackDamage = 15f;
    public boolean attackPending;
}
