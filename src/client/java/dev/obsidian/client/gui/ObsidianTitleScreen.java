package dev.obsidian.client.gui;

import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.core.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** The Obsidian main menu: big logo, wordmark and a column of buttons. */
public class ObsidianTitleScreen extends Screen {
	private static final String[] BUTTONS = { "Singleplayer", "Multiplayer", "Obsidian Client", "Options", "Quit" };
	private static final int BW = 220, BH = 24, GAP = 6;
	private final long start = System.currentTimeMillis();

	public ObsidianTitleScreen() { super(Component.literal("Obsidian Client")); }

	private int firstY() { return height / 2 - 6; }
	private int bx() { return (width - BW) / 2; }
	private int by(int i) { return firstY() + i * (BH + GAP); }

	@Override public void renderBackground(GuiGraphics g, int mx, int my, float d) {}

	private static void ellipse(GuiGraphics g, int cx, int cy, int rx, int ry, int color) {
		for (int dy = -ry; dy <= ry; dy += 2) {
			int half = (int) (rx * Math.sqrt(1.0 - (dy * (double) dy) / (ry * (double) ry)));
			g.fill(cx - half, cy + dy, cx + half, cy + dy + 2, color);
		}
	}

	@Override
	public void render(GuiGraphics g, int mx, int my, float delta) {
		float t = ThemeManager.ANIMATIONS.on() ? Draw.ease((System.currentTimeMillis() - start) / 500f) : 1f;
		double time = (System.currentTimeMillis() - start) / 1000.0;
		int acc = ThemeManager.accent();

		g.fillGradient(0, 0, width, height, ThemeManager.mix(0xFF050308, acc, 0.05f), 0xFF020104);
		int drift = ThemeManager.ANIMATIONS.on() ? (int) (Math.sin(time * 0.4) * 6) : 0;
		ellipse(g, width / 2, height / 2 + drift, (int) (width * 0.34), (int) (height * 0.46), Draw.a(acc, 0.05f));
		ellipse(g, width / 2, height / 2 - drift, (int) (width * 0.23), (int) (height * 0.32), Draw.a(acc, 0.07f));

		// logo + wordmark
		int logo = Math.min(84, height / 5);
		int ly = (int) (height / 2 - 6 - logo - 44 + (1f - t) * -10);
		MainMenuOverlay.mark(g, (width - logo) / 2, ly, logo, t);

		String word = "OBSIDIAN";
		int sc = 2, spacing = 8;
		int ww = 0;
		for (char c : word.toCharArray()) ww += font.width(String.valueOf(c)) * sc + spacing;
		ww -= spacing;
		int wx = (width - ww) / 2, wy = ly + logo + 8;
		for (char c : word.toCharArray()) {
			String s = String.valueOf(c);
			g.pose().pushMatrix();
			g.pose().translate(wx, wy);
			g.pose().scale(sc, sc);
			g.drawString(font, s, 0, 0, Draw.a(0xFFFFFFFF, t), false);
			g.pose().popMatrix();
			wx += font.width(s) * sc + spacing;
		}
		String sub = "C L I E N T";
		g.drawString(font, sub, (width - font.width(sub)) / 2, wy + 20, Draw.a(ThemeManager.accent2(), t), false);
		g.fill(width / 2 - 45, wy + 32, width / 2 + 45, wy + 33, Draw.a(acc, t));

		// buttons
		for (int i = 0; i < BUTTONS.length; i++) {
			int x = bx(), y = by(i);
			boolean hover = Draw.in(mx, my, x, y, BW, BH);
			Draw.rect(g, x, y, BW, BH, Draw.a(hover ? ThemeManager.cardHover() : ThemeManager.card(), 0.92f * t));
			if (hover) g.renderOutline(x, y, BW, BH, Draw.a(acc, t));
			String s = BUTTONS[i];
			boolean special = i == 2;
			g.drawString(font, s, x + (BW - font.width(s)) / 2, y + (BH - 8) / 2,
				Draw.a(special ? ThemeManager.accent2() : (hover ? 0xFFFFFFFF : 0xFFD6D0E4), t), false);
		}

		// top-right menu button
		int sx = width - 38, sy = 10;
		boolean hs = Draw.in(mx, my, sx, sy, 28, 28);
		Draw.rect(g, sx, sy, 28, 28, Draw.a(hs ? ThemeManager.cardHover() : ThemeManager.card(), t));
		MainMenuOverlay.mark(g, sx + 4, sy + 4, 20, t);

		// footer
		String hint = "Right Shift in game for the Obsidian menu";
		g.drawString(font, hint, (width - font.width(hint)) / 2, height - 24, Draw.a(ThemeManager.dim(), t), false);
		g.drawString(font, "Minecraft 1.21.11", 8, height - 14, Draw.a(ThemeManager.dim(), t), false);
		String ver = "Obsidian Client v" + UpdateChecker.current();
		g.drawString(font, ver, width - font.width(ver) - 8, height - 14, Draw.a(ThemeManager.dim(), t), false);
		if (UpdateChecker.updateAvailable) {
			String u = "Update available: " + UpdateChecker.latest;
			g.drawString(font, u, width - font.width(u) - 8, height - 26, 0xFF4ADE80, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
		if (e.button() != 0) return super.mouseClicked(e, dbl);
		Minecraft mc = Minecraft.getInstance();
		if (Draw.in(e.x(), e.y(), width - 38, 10, 28, 28)) { mc.setScreen(new MenuScreen()); return true; }
		for (int i = 0; i < BUTTONS.length; i++) {
			if (!Draw.in(e.x(), e.y(), bx(), by(i), BW, BH)) continue;
			switch (i) {
				case 0 -> mc.setScreen(new SelectWorldScreen(this));
				case 1 -> mc.setScreen(new JoinMultiplayerScreen(this));
				case 2 -> mc.setScreen(new MenuScreen());
				case 3 -> mc.setScreen(new OptionsScreen(this, mc.options));
				default -> mc.stop();
			}
			return true;
		}
		return super.mouseClicked(e, dbl);
	}

	@Override public boolean shouldCloseOnEsc() { return false; }
	@Override public boolean isPauseScreen() { return false; }
}
