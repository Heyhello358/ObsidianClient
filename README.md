# Obsidian Client

A legit, client-side Fabric mod for Minecraft Java 1.21.11. No cheats: no reach, no auto-clicking, no X-ray.
It only displays information you already have and adds quality-of-life tools.

![Obsidian Client](src/main/resources/assets/obsidian/logo.png)

**Gameplay:** FPS, CPS, Keystrokes, Toggle Sprint, Toggle Sneak, Zoom, Freelook, Hitboxes, Armor Status, Potion Effects, Coordinates, Direction, Ping, Clock, Memory.
**PvP (display only):** Combo, Reach Display, W-Tap Counter, Hit Delay, Damage Indicator, Target HUD, Attack Indicator.
**Visuals:** Custom Crosshair, Scoreboard customizer, Custom Main Menu logo, themes (Purple / Blue / Red / Custom colour), animated UI.
**Performance:** FPS Optimizer, Particle Reducer.
**Obsidian:** tabbed menu, HUD editor, profiles, local account stats, built-in update checker, Discord Rich Presence.

**Keys:** Right Shift menu, C zoom, Alt freelook, V toggle sprint, B toggle sneak (all rebindable). Settings are saved to `config/obsidian.json`.

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

## Obsidian roles and nametags (backend)
Other Obsidian users see a role logo on your nametag. Roles: Member, Media, Staff, High Staff, Owner. Only the admin
(UUID in `backend/wrangler.toml` and `Backend.java`) can change roles and colours, from the **ADMIN** tab in the menu.
Players prove who they are through Mojang, so nobody can fake a UUID. Your Minecraft access token never leaves your PC.

The backend is a small Cloudflare Worker in `backend/`. Setup:
1. Make a free Cloudflare account.
2. Create an API token ("Edit Cloudflare Workers" template) and copy your Account ID.
3. In this GitHub repo: Settings > Secrets and variables > Actions, add `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID`.
4. Run the "Deploy backend" workflow. Put the resulting `https://obsidian-backend.<you>.workers.dev` address into `Backend.java`.
