package dev.obsidian.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Small drawing helpers shared by the Obsidian screens. */
public final class Draw {
	private Draw() {}

	/** Multiplies the alpha of an ARGB colour. */
	public static int a(int argb, float alpha) {
		int al = (int) (((argb >>> 24) & 255) * Math.max(0f, Math.min(1f, alpha)));
		return (al << 24) | (argb & 0xFFFFFF);
	}

	/** Rectangle with chamfered corners (looks rounded at GUI scale). */
	public static void rect(GuiGraphics g, int x, int y, int w, int h, int c) {
		g.fill(x + 1, y, x + w - 1, y + h, c);
		g.fill(x, y + 1, x + 1, y + h - 1, c);
		g.fill(x + w - 1, y + 1, x + w, y + h - 1, c);
	}

	public static boolean in(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public static void text(GuiGraphics g, net.minecraft.client.gui.Font f, String s, int x, int y, int c, float alpha) {
		int col = a(c, alpha);
		if (((col >>> 24) & 255) < 5) return;
		g.drawString(f, s, x, y, col, false);
	}

	public static float ease(float t) {
		t = Math.max(0f, Math.min(1f, t));
		return 1f - (1f - t) * (1f - t) * (1f - t);
	}
}
