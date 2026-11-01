package dev.obsidian.client.module;

import java.util.ArrayList;
import java.util.List;

import dev.obsidian.client.core.Setting;
import net.minecraft.client.gui.GuiGraphics;

/** Base class for every Obsidian module. */
public abstract class Module {
	public enum Category {
		GAMEPLAY("Gameplay"), PVP("PvP"), VISUALS("Visuals"), PERFORMANCE("Performance"), CLIENT("Client");
		public final String label;
		Category(String label) { this.label = label; }
	}

	public final String id, name, description;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();

	private boolean enabled;
	public int x, y; // HUD position, GUI-scaled pixels

	protected Module(String id, String name, String description, Category category, int x, int y) {
		this.id = id; this.name = name; this.description = description;
		this.category = category; this.x = x; this.y = y;
	}

	protected Setting bool(String id, String name, boolean def) {
		Setting s = Setting.bool(id, name, def); settings.add(s); return s;
	}
	protected Setting num(String id, String name, double def, double min, double max, double step) {
		Setting s = Setting.num(id, name, def, min, max, step); settings.add(s); return s;
	}
	protected Setting mode(String id, String name, String... modes) {
		Setting s = Setting.mode(id, name, modes); settings.add(s); return s;
	}

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean value) {
		if (enabled == value) return;
		enabled = value;
		if (value) onEnable(); else onDisable();
	}
	public void toggle() { setEnabled(!enabled); }

	protected void onEnable() {}
	protected void onDisable() {}
	/** Undo any game options this module changed (called before the game saves its options on exit). */
	public void restore() {}
	public void tick() {}

	public boolean isHud() { return false; }
	public int width() { return 0; }
	public int height() { return 0; }
	public void render(GuiGraphics g) {}
}
