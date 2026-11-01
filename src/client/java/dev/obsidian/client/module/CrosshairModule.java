package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import dev.obsidian.client.core.ThemeManager;
import net.minecraft.client.gui.GuiGraphics;

/** Replaces the vanilla crosshair with a custom one. */
public class CrosshairModule extends Module {
	public static CrosshairModule instance;
	private final Setting style = mode("style", "Style", "Cross", "Plus", "Dot", "Circle");
	private final Setting size = num("size", "Size", 5, 1, 12, 1);
	private final Setting gap = num("gap", "Gap", 2, 0, 8, 1);
	private final Setting thick = num("thickness", "Thickness", 1, 1, 4, 1);
	private final Setting outline = bool("outline", "Outline", true);
	private final Setting colour = mode("colour", "Colour", "Theme", "White", "Green", "Red");

	public CrosshairModule() {
		super("crosshair", "Custom Crosshair", "Pick a style, size and colour", Category.VISUALS, 0, 0);
		instance = this;
	}

	private int rgb() {
		return switch (colour.intValue()) {
			case 1 -> 0xFFFFFFFF;
			case 2 -> 0xFF4ADE80;
			case 3 -> 0xFFEF4444;
			default -> ThemeManager.accent();
		};
	}

	private void rect(GuiGraphics g, int x1, int y1, int x2, int y2) {
		if (outline.on()) g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
		g.fill(x1, y1, x2, y2, rgb());
	}

	public void draw(GuiGraphics g) {
		int cx = g.guiWidth() / 2, cy = g.guiHeight() / 2;
		int s = size.intValue(), gp = gap.intValue(), t = thick.intValue();
		int h = t / 2;
		switch (style.intValue()) {
			case 2 -> rect(g, cx - t, cy - t, cx + t, cy + t);
			case 3 -> {
				int r = s + 2;
				for (int a = 0; a < 40; a++) {
					double ang = a * Math.PI * 2 / 40;
					int px = cx + (int) Math.round(Math.cos(ang) * r), py = cy + (int) Math.round(Math.sin(ang) * r);
					g.fill(px, py, px + t, py + t, rgb());
				}
			}
			case 1 -> {
				rect(g, cx - s, cy - h, cx + s + 1, cy - h + t);
				rect(g, cx - h, cy - s, cx - h + t, cy + s + 1);
			}
			default -> {
				rect(g, cx - gp - s, cy - h, cx - gp, cy - h + t);
				rect(g, cx + gp + 1, cy - h, cx + gp + 1 + s, cy - h + t);
				rect(g, cx - h, cy - gp - s, cx - h + t, cy - gp);
				rect(g, cx - h, cy + gp + 1, cx - h + t, cy + gp + 1 + s);
			}
		}
	}
}
