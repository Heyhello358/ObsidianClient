package dev.obsidian.client.core;

import java.util.ArrayList;
import java.util.List;

/** Colours for the whole client. Presets: Obsidian Purple, Ocean Blue, Crimson Red, plus a custom colour. */
public final class ThemeManager {
	public record Theme(String name, int accent, int accent2) {}

	public static final Theme[] PRESETS = {
		new Theme("Obsidian Purple", 0x8B5CF6, 0xC4A5FF),
		new Theme("Ocean Blue", 0x3B82F6, 0x93C5FD),
		new Theme("Crimson Red", 0xEF4444, 0xFCA5A5),
	};
	public static final String CUSTOM = "Custom";

	public static String current = PRESETS[0].name();

	public static final Setting ANIMATIONS = Setting.bool("animations", "Animated UI", true);
	public static final Setting HUD_OPACITY = Setting.num("hudOpacity", "HUD background", 55, 0, 100, 5);
	public static final Setting CUSTOM_R = Setting.num("customR", "Custom red", 139, 0, 255, 1);
	public static final Setting CUSTOM_G = Setting.num("customG", "Custom green", 92, 0, 255, 1);
	public static final Setting CUSTOM_B = Setting.num("customB", "Custom blue", 246, 0, 255, 1);

	/** Everything that is saved globally (not per module). */
	public static List<Setting> all() {
		return List.of(ANIMATIONS, HUD_OPACITY, CUSTOM_R, CUSTOM_G, CUSTOM_B);
	}
	public static List<Setting> colourSettings() { return List.of(CUSTOM_R, CUSTOM_G, CUSTOM_B); }
	public static List<Setting> generalSettings() { return List.of(ANIMATIONS, HUD_OPACITY); }

	private ThemeManager() {}

	private static int baseRgb() {
		if (CUSTOM.equals(current)) return (CUSTOM_R.intValue() << 16) | (CUSTOM_G.intValue() << 8) | CUSTOM_B.intValue();
		for (Theme t : PRESETS) if (t.name().equals(current)) return t.accent();
		return PRESETS[0].accent();
	}

	public static int accent() { return 0xFF000000 | baseRgb(); }
	public static int accent2() { return mix(accent(), 0xFFFFFFFF, 0.45f); }
	public static int bg() { return mix(0xFF07050B, accent(), 0.07f); }
	public static int card() { return mix(0xFF120D1A, accent(), 0.10f); }
	public static int cardHover() { return mix(0xFF120D1A, accent(), 0.20f); }
	public static int dim() { return 0xFF9A93AD; }
	public static int hudBg() { return ((int) (HUD_OPACITY.num() * 2.55) << 24); }

	public static int mix(int a, int b, float t) {
		int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
		int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
		int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}

	public static List<String> themeNames() {
		List<String> l = new ArrayList<>();
		for (Theme t : PRESETS) l.add(t.name());
		l.add(CUSTOM);
		return l;
	}
	public static int swatch(String name) {
		for (Theme t : PRESETS) if (t.name().equals(name)) return 0xFF000000 | t.accent();
		return 0xFF000000 | ((CUSTOM_R.intValue() << 16) | (CUSTOM_G.intValue() << 8) | CUSTOM_B.intValue());
	}
}
