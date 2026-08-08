package com.github.shahamatirtisham.promise_beneath_the_storm.state;

/** A snapshot of the persistent run state restored after player death. */
public class RunCheckpoint {
    public int restartLevel;
    public boolean bossCheckpoint;
    public float maximumHealth;
    public int devilCoins;
    public int enemiesDefeated;
    public int ironHeart;
    public int stormEdge;
    public int windstepSigil;
    public float attackDamage;
    public float dashCooldown;
    public int knives;
    public int knifeCapacity;

    public void capture(
        int restartLevel,
        boolean bossCheckpoint,
        float maximumHealth,
        int devilCoins,
        int enemiesDefeated,
        int ironHeart,
        int stormEdge,
        int windstepSigil,
        float attackDamage,
        float dashCooldown,
        int knives,
        int knifeCapacity
    ) {
        this.restartLevel = restartLevel;
        this.bossCheckpoint = bossCheckpoint;
        this.maximumHealth = maximumHealth;
        this.devilCoins = devilCoins;
        this.enemiesDefeated = enemiesDefeated;
        this.ironHeart = ironHeart;
        this.stormEdge = stormEdge;
        this.windstepSigil = windstepSigil;
        this.attackDamage = attackDamage;
        this.dashCooldown = dashCooldown;
        this.knives = knives;
        this.knifeCapacity = knifeCapacity;
    }
}
