package dev.obsidian.client.module;

import net.minecraft.client.gui.GuiGraphics;

/** Base class for every Obsidian module. */
public abstract class Module {
	public enum Category { HUD, PLAYER, WORLD, CLIENT }

	public final String id;
	public final String name;
	public final String description;
	public final Category category;

	private boolean enabled;
	/** HUD position in GUI-scaled pixels. */
	public int x, y;

	protected Module(String id, String name, String description, Category category, int x, int y) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.x = x;
		this.y = y;
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

	/** Called every client tick while enabled. */
	public void tick() {}

	/** True if this module draws something on the HUD. */
	public boolean isHud() { return false; }
	public int width() { return 0; }
	public int height() { return 0; }
	public void render(GuiGraphics g) {}
}
