package dev.obsidian.client.config;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.core.Setting;
import dev.obsidian.client.core.Stats;
import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

/** Saves modules, theme and stats to config/obsidian.json. Profiles live in config/obsidian/profiles/. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path DIR = FabricLoader.getInstance().getConfigDir();
	private static final Path FILE = DIR.resolve("obsidian.json");
	private static final Path PROFILES = DIR.resolve("obsidian").resolve("profiles");

	public static String activeProfile = "";

	private static class Entry { boolean enabled; int x, y; Map<String, Double> settings = new LinkedHashMap<>(); }
	private static class StatsData { long playSeconds, hits, clicks, bestCombo; }
	private static class Data {
		String theme;
		String profile;
		Map<String, Double> globals = new LinkedHashMap<>();
		Map<String, Entry> modules = new LinkedHashMap<>();
		StatsData stats;
	}

	private Config() {}

	private static Data snapshot(boolean withStats) {
		Data d = new Data();
		d.theme = ThemeManager.current;
		d.profile = activeProfile;
		for (Setting s : ThemeManager.all()) d.globals.put(s.id, s.value);
		for (Module m : ModuleManager.MODULES) {
			Entry e = new Entry();
			e.enabled = m.isEnabled(); e.x = m.x; e.y = m.y;
			for (Setting s : m.settings) e.settings.put(s.id, s.value);
			d.modules.put(m.id, e);
		}
		if (withStats) {
			d.stats = new StatsData();
			d.stats.playSeconds = Stats.playSeconds; d.stats.hits = Stats.hits;
			d.stats.clicks = Stats.clicks; d.stats.bestCombo = Stats.bestCombo;
		}
		return d;
	}

	private static void apply(Data d, boolean withStats) {
		if (d == null) return;
		if (d.theme != null) ThemeManager.current = d.theme;
		if (d.globals != null) {
			for (Setting s : ThemeManager.all()) {
				Double v = d.globals.get(s.id);
				if (v != null) s.set(v);
			}
		}
		if (d.modules != null) {
			d.modules.forEach((id, e) -> {
				Module m = ModuleManager.get(id);
				if (m == null || e == null) return;
				m.x = e.x; m.y = e.y;
				if (e.settings != null) {
					for (Setting s : m.settings) {
						Double v = e.settings.get(s.id);
						if (v != null) s.set(v);
					}
				}
				m.setEnabled(e.enabled);
			});
		}
		if (withStats && d.stats != null) {
			Stats.playSeconds = d.stats.playSeconds; Stats.hits = d.stats.hits;
			Stats.clicks = d.stats.clicks; Stats.bestCombo = d.stats.bestCombo;
		}
	}

	private static Data read(Path p) {
		try (Reader r = Files.newBufferedReader(p)) {
			return GSON.fromJson(r, Data.class);
		} catch (Exception ex) {
			System.err.println("[Obsidian] Could not read " + p + ": " + ex);
			return null;
		}
	}

	private static void write(Path p, Data d) {
		try {
			Files.createDirectories(p.getParent());
			try (Writer w = Files.newBufferedWriter(p)) { GSON.toJson(d, w); }
		} catch (Exception ex) {
			System.err.println("[Obsidian] Could not save " + p + ": " + ex);
		}
	}

	public static void load() {
		if (!Files.exists(FILE)) return;
		Data d = read(FILE);
		if (d != null && d.profile != null) activeProfile = d.profile;
		apply(d, true);
	}

	public static void save() { write(FILE, snapshot(true)); }

	// ---- profiles ----

	public static List<String> profiles() {
		List<String> out = new ArrayList<>();
		if (!Files.isDirectory(PROFILES)) return out;
		try (Stream<Path> s = Files.list(PROFILES)) {
			s.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".json"))
				.map(n -> n.substring(0, n.length() - 5)).sorted().forEach(out::add);
		} catch (Exception ignored) {}
		return out;
	}

	public static String newProfile() {
		List<String> existing = profiles();
		int i = 1;
		while (existing.contains("Profile " + i)) i++;
		String name = "Profile " + i;
		write(PROFILES.resolve(name + ".json"), snapshot(false));
		activeProfile = name;
		save();
		return name;
	}

	public static void loadProfile(String name) {
		Path p = PROFILES.resolve(name + ".json");
		if (!Files.exists(p)) return;
		apply(read(p), false);
		activeProfile = name;
		save();
	}

	public static void overwriteProfile(String name) { write(PROFILES.resolve(name + ".json"), snapshot(false)); }

	public static void deleteProfile(String name) {
		try { Files.deleteIfExists(PROFILES.resolve(name + ".json")); } catch (Exception ignored) {}
		if (name.equals(activeProfile)) activeProfile = "";
		save();
	}
}
