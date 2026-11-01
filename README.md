# Obsidian Client

A legit, client-side Fabric mod for Minecraft Java 1.21.11. No cheats: no reach, no auto-clicking, no X-ray.
It only displays information you already have and adds quality-of-life tools.

![Obsidian Client](src/main/resources/assets/obsidian/logo.png)

**Modules:** FPS, CPS, Coordinates, Direction, Ping, Clock, Keystrokes, Armor Status, Zoom.

**Keys:** Right Shift opens the menu (click a card to toggle it, "Edit HUD" to drag things around). C zooms (rebindable).
Settings are saved to `config/obsidian.json`.

## Discord Rich Presence
Shows "Obsidian Client" on your Discord profile with a **Join Discord** button (https://discord.gg/XAj6Xb9t9A).
One-time setup:
1. Go to https://discord.com/developers/applications, create an application named **Obsidian Client**.
2. Copy its *Application ID* into `APP_ID` in `DiscordRpcModule.java` (already set to 1566475880669380678).
3. Under *Rich Presence > Art Assets*, upload `icon.png` with the name `logo`.

Discord hides your own buttons from you; other people see them. Toggle it with the "Discord RPC" module.

## Build (easiest: GitHub)
Push this folder to a GitHub repo. The Actions tab builds it automatically; download `ObsidianClient-jar` from the finished run.

## Build locally
Needs JDK 25 to run the build (the mod itself targets Java 21) and internet access to Mojang/Fabric servers.

    ./gradlew build

The jar is in `build/libs/` (use the one without `-sources`). Drop it in `.minecraft/mods/` with Fabric Loader and Fabric API for 1.21.11.
Run from source with `./gradlew runClient`.

## Status
Source is written against the 1.21.11 Mojang-mapped API but has NOT been compiled yet. If `./gradlew build`
reports a renamed method, it is a small fix.
