package dev.obsidian.client.config;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

/** Saves module on/off state and HUD positions to config/obsidian.json. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("obsidian.json");

	private static class Entry { boolean enabled; int x, y; }
	private static class Data { Map<String, Entry> modules = new LinkedHashMap<>(); }

	private Config() {}

	public static void load() {
		if (!Files.exists(FILE)) return;
		try (Reader r = Files.newBufferedReader(FILE)) {
			Data d = GSON.fromJson(r, Data.class);
			if (d == null || d.modules == null) return;
			d.modules.forEach((id, e) -> {
				Module m = ModuleManager.get(id);
				if (m == null || e == null) return;
				m.x = e.x; m.y = e.y;
				m.setEnabled(e.enabled);
			});
		} catch (Exception ex) {
			System.err.println("[Obsidian] Could not read config: " + ex);
		}
	}

	public static void save() {
		Data d = new Data();
		for (Module m : ModuleManager.MODULES) {
			Entry e = new Entry();
			e.enabled = m.isEnabled(); e.x = m.x; e.y = m.y;
			d.modules.put(m.id, e);
		}
		try (Writer w = Files.newBufferedWriter(FILE)) {
			GSON.toJson(d, w);
		} catch (Exception ex) {
			System.err.println("[Obsidian] Could not save config: " + ex);
		}
	}
}
