package dev.obsidian.client;

import java.util.ArrayList;
import java.util.List;

import dev.obsidian.client.module.*;
import dev.obsidian.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class ModuleManager {
	public static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {}

	public static void init() {
		Minecraft mc = Minecraft.getInstance();
		MODULES.add(new TextModule("fps", "FPS", "Shows your frames per second", 4, 4,
			() -> mc.getFps() + " FPS"));
		MODULES.add(new CpsModule(4, 20));
		MODULES.add(new TextModule("coords", "Coordinates", "Shows your position", 4, 36, () -> {
			Player p = mc.player;
			return p == null ? "XYZ: -" : String.format("XYZ: %d %d %d",
				Mth.floor(p.getX()), Mth.floor(p.getY()), Mth.floor(p.getZ()));
		}));
		MODULES.add(new TextModule("direction", "Direction", "Shows the way you are facing", 4, 52, () ->
			mc.player == null ? "-" : mc.player.getDirection().getName().toUpperCase()));
		MODULES.add(new TextModule("ping", "Ping", "Shows your server latency", 4, 68, () -> {
			if (mc.player == null || mc.getConnection() == null) return "- ms";
			var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
			return (info == null ? 0 : info.getLatency()) + " ms";
		}));
		MODULES.add(new TextModule("clock", "Clock", "Shows your local time", 4, 84, () ->
			java.time.LocalTime.now().withNano(0).toString()));
		MODULES.add(new KeystrokesModule(4, 104));
		MODULES.add(new ArmorModule(4, 172));
		MODULES.add(new ZoomModule());
		MODULES.add(new DiscordRpcModule());
	}

	public static Module get(String id) {
		for (Module m : MODULES) if (m.id.equals(id)) return m;
		return null;
	}

	public static void tick() {
		for (Module m : MODULES) if (m.isEnabled()) m.tick();
	}

	public static void renderHud(GuiGraphics g) {
		for (Module m : MODULES) if (m.isEnabled() && m.isHud()) m.render(g);
	}
}
