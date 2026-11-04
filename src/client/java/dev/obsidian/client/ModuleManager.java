package dev.obsidian.client;

import java.util.ArrayList;
import java.util.List;

import dev.obsidian.client.core.CombatState;
import dev.obsidian.client.module.*;
import dev.obsidian.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class ModuleManager {
	public static final List<Module> MODULES = new ArrayList<>();
	private static final Module.Category G = Module.Category.GAMEPLAY, P = Module.Category.PVP,
		PERF = Module.Category.PERFORMANCE;

	private ModuleManager() {}

	public static void init() {
		Minecraft mc = Minecraft.getInstance();
		int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();

		// Gameplay
		MODULES.add(new TextModule("fps", "FPS Counter", "Shows your frames per second", G, 4, 4, () -> mc.getFps() + " FPS"));
		MODULES.add(new CpsModule(4, 20));
		MODULES.add(new KeystrokesModule(4, 40));
		MODULES.add(new ToggleSprintModule(4, 130));
		MODULES.add(new ToggleSneakModule(4, 146));
		MODULES.add(new ZoomModule());
		MODULES.add(new FreelookModule());
		MODULES.add(new HitboxesModule());
		MODULES.add(new ArmorModule(sw - 60, sh / 2 - 45));
		MODULES.add(new PotionModule(sw - 110, 4));
		MODULES.add(new TextModule("coords", "Coordinates", "Shows your position", G, 70, 4, () -> {
			Player p = mc.player;
			return p == null ? "XYZ: -" : String.format("XYZ: %d %d %d", Mth.floor(p.getX()), Mth.floor(p.getY()), Mth.floor(p.getZ()));
		}));
		MODULES.add(new TextModule("direction", "Direction HUD", "Shows the way you are facing", G, 70, 20, () ->
			mc.player == null ? "-" : mc.player.getDirection().getName().toUpperCase()));
		MODULES.add(new TextModule("ping", "Ping Display", "Shows your server latency", G, 70, 36, () -> {
			if (mc.player == null || mc.getConnection() == null) return "- ms";
			var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
			return (info == null ? 0 : info.getLatency()) + " ms";
		}));
		MODULES.add(new TextModule("clock", "Clock", "Shows your local time", G, 70, 52, () ->
			java.time.LocalTime.now().withNano(0).toString()));

		// PvP (display only - nothing here changes your attacks)
		MODULES.add(new TextModule("combo", "Combo Counter", "Hits in a row without being hit", P, 4, 170, () -> "Combo: " + CombatState.combo));
		MODULES.add(new TextModule("reach", "Reach Display", "Distance of your last hit", P, 4, 186, () -> String.format("Reach: %.2f", CombatState.lastReach)));
		MODULES.add(new TextModule("wtap", "W-Tap Counter", "Counts your W-taps after hits", P, 4, 202, () -> "W-Taps: " + CombatState.wtaps));
		MODULES.add(new TextModule("hitdelay", "Hit Delay", "Time between your last two hits", P, 4, 218, () -> "Hit delay: " + CombatState.lastHitInterval + "ms"));
		MODULES.add(new TextModule("damage", "Damage Indicator", "Damage of your last hit", P, 4, 234, () -> String.format("Damage: %.1f", CombatState.lastDamage)));
		MODULES.add(new TargetHudModule(sw / 2 + 20, sh / 2 + 20));
		MODULES.add(new AttackIndicatorModule(sw / 2 - 20, sh / 2 + 12));

		// Visuals
		MODULES.add(new CrosshairModule());
		MODULES.add(new ScoreboardModule());
		MODULES.add(new FlagModule("mainmenu", "Custom Main Menu", "Obsidian title screen with logo and buttons", Module.Category.VISUALS));

		// Performance
		MODULES.add(new FpsOptimizerModule());
		MODULES.add(new ParticleReducerModule());
		MODULES.add(new TextModule("memory", "Memory Usage", "Shows used / max memory", PERF, 70, 68, () -> {
			Runtime r = Runtime.getRuntime();
			return (r.totalMemory() - r.freeMemory()) / 1048576 + " / " + r.maxMemory() / 1048576 + " MB";
		}));

		// Client
		MODULES.add(new DiscordRpcModule());
		MODULES.add(new NametagModule());

		// Sensible defaults
		for (String id : new String[] { "fps", "cps", "keystrokes", "togglesprint", "zoom", "coords", "armor", "mainmenu", "discord", "freelook", "nametags" })
			get(id).setEnabled(true);
	}

	public static Module get(String id) {
		for (Module m : MODULES) if (m.id.equals(id)) return m;
		return null;
	}

	public static void tick() {
		for (Module m : MODULES) if (m.isEnabled()) m.tick();
	}

	public static void restoreOptions() {
		for (Module m : MODULES) m.restore();
	}

	public static void renderHud(GuiGraphics g) {
		for (Module m : MODULES) if (m.isEnabled() && m.isHud()) m.render(g);
	}
}
