package dev.obsidian.client.gui;

import java.util.List;

import dev.obsidian.client.core.Setting;
import dev.obsidian.client.core.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Draws and edits a list of settings: switches, sliders and choice buttons. */
public class SettingList {
	public static final int ROW = 22;
	private final List<Setting> list;
	private int x, y, w;
	private Setting dragging;

	public SettingList(List<Setting> list) { this.list = list; }

	public int height() { return list.size() * ROW; }

	public void render(GuiGraphics g, int x, int y, int w, int mx, int my, float al) {
		this.x = x; this.y = y; this.w = w;
		Font f = Minecraft.getInstance().font;
		int ry = y;
		for (Setting s : list) {
			boolean hover = Draw.in(mx, my, x, ry, w, ROW - 2);
			Draw.rect(g, x, ry, w, ROW - 2, Draw.a(hover ? ThemeManager.cardHover() : ThemeManager.card(), al));
			Draw.text(g, f, s.name, x + 8, ry + 7, 0xFFFFFFFF, al);
			int rx = x + w;
			switch (s.type) {
				case BOOL -> sw(g, rx - 32, ry + 4, s.on() ? 1f : 0f, al);
				case MODE -> {
					String t = "< " + s.modeName() + " >";
					Draw.text(g, f, t, rx - 8 - f.width(t), ry + 7, ThemeManager.accent2(), al);
				}
				case NUM -> {
					int tx = x + w / 2, tw = w / 2 - 54;
					g.fill(tx, ry + 9, tx + tw, ry + 11, Draw.a(0xFF3A3346, al));
					double f01 = (s.num() - s.min) / (s.max - s.min);
					g.fill(tx, ry + 9, tx + (int) (tw * f01), ry + 11, Draw.a(ThemeManager.accent(), al));
					int kx = tx + (int) (tw * f01);
					g.fill(kx - 2, ry + 5, kx + 2, ry + 15, Draw.a(0xFFFFFFFF, al));
					String v = s.display();
					Draw.text(g, f, v, rx - 8 - f.width(v), ry + 7, ThemeManager.dim(), al);
				}
			}
			ry += ROW;
		}
	}

	static void sw(GuiGraphics g, int x, int y, float t, float al) {
		int track = ThemeManager.mix(0xFF3A3346, ThemeManager.accent(), t);
		Draw.rect(g, x, y, 24, 12, Draw.a(track, al));
		int kx = x + 2 + (int) (12 * t);
		g.fill(kx, y + 2, kx + 8, y + 10, Draw.a(0xFFFFFFFF, al));
	}

	public boolean click(double mx, double my, int button) {
		int ry = y;
		for (Setting s : list) {
			if (Draw.in(mx, my, x, ry, w, ROW - 2)) {
				switch (s.type) {
					case BOOL -> s.toggle();
					case MODE -> s.cycle(button == 1 ? -1 : 1);
					case NUM -> { dragging = s; drag(mx); }
				}
				return true;
			}
			ry += ROW;
		}
		return false;
	}

	public void drag(double mx) {
		if (dragging == null) return;
		int tx = x + w / 2, tw = w / 2 - 54;
		double f = Math.max(0, Math.min(1, (mx - tx) / tw));
		dragging.set(dragging.min + f * (dragging.max - dragging.min));
	}

	public boolean dragging() { return dragging != null; }
	public void release() { dragging = null; }
}
