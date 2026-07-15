# Promise Beneath the Storm

A top-down roguelike dungeon crawler built in Java for the CSE 4402 Visual Programming Lab.

Mashkam enters a corrupted dungeon searching for his childhood friend, Irhos. After six dungeon levels, he faces the Devil Monarch through three armor phases - Iron Fist, Burning Gauntlets, and Devil's Crown - before the final reveal and main fight.

## Current milestone

The project is in its foundation stage. The current prototype includes:

- A LibGDX desktop application
- Ashley ECS entities, components, and systems
- WASD movement with normalized diagonal speed
- Box2D player physics and room-wall collision
- A camera that follows the player
- Placeholder debug rendering

The next milestone is a vertical slice containing one playable room, one enemy, basic mouse-directed combat, enemy death, and an exit door.

## Planned game

- Six themed dungeon levels
- Procedurally arranged handcrafted room templates
- Checkpoints after Levels 3 and 6
- Collectables, upgrades, memory shards, and door hints
- Three Devil Monarch armor phases followed by the Irhos reveal and final fight
- Branching endings

Spring Boot accounts and leaderboards are a stretch goal after the playable game is complete.

## Team

- Irtisham - engine and architecture
- Abtahi - gameplay systems and balancing
- Arkam - visual design, sprites, shaders, and polish

## Run the desktop prototype

On Windows:

```powershell
.\gradlew.bat lwjgl3:run
```

Build without launching:

```powershell
.\gradlew.bat build
```
