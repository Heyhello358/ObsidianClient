package dev.obsidian.client.gui;

import dev.obsidian.client.config.Config;
import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Settings for one module. Right-click a module card to open. */
public class ModuleSettingsScreen extends Screen {
	private final Screen parent;
	private final Module module;
	private final SettingList list;

	public ModuleSettingsScreen(Screen parent, Module module) {
		super(Component.literal(module.name));
		this.parent = parent; this.module = module;
		this.list = new SettingList(module.settings);
	}

	private int pw() { return Math.min(width - 16, 300); }
	private int px() { return (width - pw()) / 2; }
	private int ph() { return list.height() + 70; }
	private int py() { return (height - ph()) / 2; }

	@Override public void renderBackground(GuiGraphics g, int mx, int my, float d) {}

	@Override
	public void render(GuiGraphics g, int mx, int my, float delta) {
		g.fill(0, 0, width, height, 0x99000000);
		int x = px(), y = py(), w = pw(), h = ph();
		Draw.rect(g, x - 1, y - 1, w + 2, h + 2, Draw.a(ThemeManager.accent(), 0.4f));
		Draw.rect(g, x, y, w, h, ThemeManager.bg());
		g.fillGradient(x + 1, y + 1, x + w - 1, y + 30, Draw.a(ThemeManager.accent(), 0.2f), 0);
		g.drawString(font, module.name.toUpperCase(), x + 12, y + 11, ThemeManager.accent2(), false);
		list.render(g, x + 8, y + 36, w - 16, mx, my, 1f);
		int by = y + h - 24;
		boolean hb = Draw.in(mx, my, x + 8, by, 70, 18);
		Draw.rect(g, x + 8, by, 70, 18, hb ? ThemeManager.cardHover() : ThemeManager.card());
		g.drawString(font, "< Back", x + 24, by + 5, 0xFFFFFFFF, false);
		if (module.isHud()) {
			boolean hr = Draw.in(mx, my, x + w - 118, by, 110, 18);
			Draw.rect(g, x + w - 118, by, 110, 18, hr ? ThemeManager.cardHover() : ThemeManager.card());
			g.drawString(font, "Edit position", x + w - 100, by + 5, 0xFFFFFFFF, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
		int x = px(), y = py(), w = pw(), by = y + ph() - 24;
		if (list.click(e.x(), e.y(), e.button())) return true;
		if (Draw.in(e.x(), e.y(), x + 8, by, 70, 18)) { onClose(); return true; }
		if (module.isHud() && Draw.in(e.x(), e.y(), x + w - 118, by, 110, 18)) { minecraft.setScreen(new HudEditorScreen(this)); return true; }
		return super.mouseClicked(e, dbl);
	}
	@Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { list.drag(e.x()); return true; }
	@Override public boolean mouseReleased(MouseButtonEvent e) { list.release(); return true; }
	@Override public boolean keyPressed(KeyEvent e) {
		if (e.key() == 256) { onClose(); return true; }
		return super.keyPressed(e);
	}
	@Override public void onClose() { Config.save(); minecraft.setScreen(parent); }
	@Override public boolean isPauseScreen() { return false; }
}
