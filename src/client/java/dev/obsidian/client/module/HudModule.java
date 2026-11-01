package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.gui.HudEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A module that draws a box on the HUD. Provides background / accent bar / colour settings. */
public abstract class HudModule extends Module {
	protected final Setting background = bool("background", "Background", true);
	protected final Setting accentBar = bool("accentBar", "Accent bar", false);
	protected final Setting shadow = bool("shadow", "Text shadow", true);
	protected final Setting textColor = mode("textColor", "Text colour", "White", "Accent");

	protected HudModule(String id, String name, String description, Category category, int x, int y) {
		super(id, name, description, category, x, y);
	}

	@Override public boolean isHud() { return true; }

	protected static boolean editing() { return Minecraft.getInstance().screen instanceof HudEditorScreen; }

	protected int color() { return textColor.intValue() == 1 ? ThemeManager.accent2() : 0xFFFFFFFF; }

	protected void box(GuiGraphics g, int w, int h) {
		if (background.on()) g.fill(x, y, x + w, y + h, ThemeManager.hudBg());
		if (accentBar.on()) g.fill(x, y, x + 2, y + h, ThemeManager.accent());
	}

	protected void text(GuiGraphics g, String s, int tx, int ty, int color) {
		g.drawString(Minecraft.getInstance().font, s, tx, ty, color, shadow.on());
	}
}
