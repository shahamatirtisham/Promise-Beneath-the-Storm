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
        float dashCooldown
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
    }
}
