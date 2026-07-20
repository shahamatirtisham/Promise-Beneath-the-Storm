package com.github.shahamatirtisham.promise_beneath_the_storm.state;

/** A snapshot of the persistent run state restored after player death. */
public class RunCheckpoint {
    public int restartLevel;
    public boolean bossCheckpoint;
    public float maximumHealth;
    public int devilCoins;
    public int enemiesDefeated;

    public void capture(
        int restartLevel,
        boolean bossCheckpoint,
        float maximumHealth,
        int devilCoins,
        int enemiesDefeated
    ) {
        this.restartLevel = restartLevel;
        this.bossCheckpoint = bossCheckpoint;
        this.maximumHealth = maximumHealth;
        this.devilCoins = devilCoins;
        this.enemiesDefeated = enemiesDefeated;
    }
}
