package dev.obsidian.client.gui;

import java.util.ArrayList;
import java.util.List;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.config.Config;
import dev.obsidian.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Main Obsidian menu: category tabs plus a grid of module cards. Click a card to toggle it. */
public class ModuleScreen extends Screen {
	private static final int CARD_W = 140, CARD_H = 44, GAP = 8, COLS = 3;
	private static final int ACCENT = 0xFF8B5CF6, BG = 0xE0101015, CARD = 0xFF1B1B24;

	private Module.Category filter = null; // null = all
	private final List<int[]> tabRects = new ArrayList<>();

	public ModuleScreen() { super(Component.literal("Obsidian Client")); }

	private int left() { return (width - (COLS * CARD_W + (COLS - 1) * GAP)) / 2; }

	private List<Module> visible() {
		List<Module> out = new ArrayList<>();
		for (Module m : ModuleManager.MODULES) if (filter == null || m.category == filter) out.add(m);
		return out;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
		g.fill(0, 0, width, height, 0x80000000);
		int l = left(), panelW = COLS * CARD_W + (COLS - 1) * GAP;
		g.fill(l - 12, 14, l + panelW + 12, height - 14, BG);
		g.drawString(font, "OBSIDIAN CLIENT", l, 24, ACCENT, true);

		// tabs
		tabRects.clear();
		int tx = l, ty = 42;
		List<Module.Category> tabs = new ArrayList<>();
		tabs.add(null);
		for (Module.Category c : Module.Category.values()) tabs.add(c);
		for (Module.Category c : tabs) {
			String label = c == null ? "All" : c.name();
			int w = font.width(label) + 14;
			boolean active = c == filter;
			g.fill(tx, ty, tx + w, ty + 16, active ? ACCENT : CARD);
			g.drawString(font, label, tx + 7, ty + 4, 0xFFFFFFFF, false);
			tabRects.add(new int[] { tx, ty, w, 16, c == null ? -1 : c.ordinal() });
			tx += w + 4;
		}

		// cards
		int i = 0;
		for (Module m : visible()) {
			int cx = l + (i % COLS) * (CARD_W + GAP), cy = 68 + (i / COLS) * (CARD_H + GAP);
			boolean hover = mouseX >= cx && mouseX < cx + CARD_W && mouseY >= cy && mouseY < cy + CARD_H;
			g.fill(cx, cy, cx + CARD_W, cy + CARD_H, hover ? 0xFF262633 : CARD);
			g.drawString(font, m.name, cx + 8, cy + 7, 0xFFFFFFFF, false);
			g.drawString(font, m.category.name(), cx + 8, cy + 19, 0xFF8888AA, false);
			int barColor = m.isEnabled() ? 0xFF3FB950 : 0xFF6E3B3B;
			g.fill(cx, cy + CARD_H - 12, cx + CARD_W, cy + CARD_H, barColor);
			String s = m.isEnabled() ? "ENABLED" : "DISABLED";
			g.drawString(font, s, cx + (CARD_W - font.width(s)) / 2, cy + CARD_H - 10, 0xFFFFFFFF, false);
			i++;
		}

		// Edit HUD button
		int bx = l, by = height - 40, bw = 90;
		g.fill(bx, by, bx + bw, by + 18, CARD);
		g.drawString(font, "Edit HUD", bx + 8, by + 5, 0xFFFFFFFF, false);
		long on = ModuleManager.MODULES.stream().filter(Module::isEnabled).count();
		g.drawString(font, on + " of " + ModuleManager.MODULES.size() + " enabled",
			bx + bw + 10, by + 5, 0xFF8888AA, false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x(), my = event.y();
		for (int[] t : tabRects) {
			if (mx >= t[0] && mx < t[0] + t[2] && my >= t[1] && my < t[1] + t[3]) {
				filter = t[4] < 0 ? null : Module.Category.values()[t[4]];
				return true;
			}
		}
		int l = left(), i = 0;
		for (Module m : visible()) {
			int cx = l + (i % COLS) * (CARD_W + GAP), cy = 68 + (i / COLS) * (CARD_H + GAP);
			if (mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H) {
				m.toggle();
				Config.save();
				return true;
			}
			i++;
		}
		int by = height - 40;
		if (mx >= l && mx < l + 90 && my >= by && my < by + 18) {
			minecraft.setScreen(new HudEditorScreen(this));
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override public boolean isPauseScreen() { return false; }
}
