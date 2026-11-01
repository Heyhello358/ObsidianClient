package dev.obsidian.client.gui;

import com.mojang.blaze3d.platform.InputConstants;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.core.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Draws the Obsidian logo (menu header and title screen). */
public final class MainMenuOverlay {
	public static final Identifier MARK = Identifier.fromNamespaceAndPath("obsidian", "textures/gui/mark.png");

	private MainMenuOverlay() {}

	public static void mark(GuiGraphics g, int x, int y, int size, float alpha) {
		g.blit(RenderPipelines.GUI_TEXTURED, MARK, x, y, 0f, 0f, size, size, 256, 256, 256, 256);
	}

	/** Called after the title screen renders. */
	public static void renderTitle(GuiGraphics g, int w, int h) {
		var m = ModuleManager.get("mainmenu");
		if (m == null || !m.isEnabled()) return;
		Minecraft mc = Minecraft.getInstance();
		var f = mc.font;
		int size = 40;
		mark(g, 10, 10, size, 1f);
		g.drawString(f, "OBSIDIAN CLIENT", 56, 16, ThemeManager.accent2(), true);
		g.drawString(f, "v" + UpdateChecker.current() + "  -  Right Shift for menu", 56, 28, ThemeManager.dim(), false);
		if (UpdateChecker.updateAvailable) g.drawString(f, "Update available: " + UpdateChecker.latest, 56, 40, 0xFF4ADE80, false);
	}
}
