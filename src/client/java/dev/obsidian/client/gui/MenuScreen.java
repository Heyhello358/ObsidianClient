package dev.obsidian.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.ObsidianClient;
import dev.obsidian.client.config.Config;
import dev.obsidian.client.core.Stats;
import dev.obsidian.client.core.ThemeManager;
import dev.obsidian.client.core.UpdateChecker;
import dev.obsidian.client.module.DiscordRpcModule;
import dev.obsidian.client.module.Module;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** The Obsidian menu: tabs on top, categories on the left, modules in the middle. */
public class MenuScreen extends Screen {
	private enum Tab {
		MODULES("MODULES"), HUD("HUD"), PROFILES("PROFILES"), THEMES("THEMES"), ACCOUNT("ACCOUNT"), SETTINGS("SETTINGS");
		final String label;
		Tab(String l) { label = l; }
	}

	private static final int HEADER = 36, SIDE = 100, ROWH = 28, GAP = 6;
	private static Tab tab = Tab.MODULES;
	private static Module.Category cat = null;

	private EditBox search;
	private double scroll;
	private long opened;
	private int px, py, pw, ph;
	private final Map<Module, Float> switches = new HashMap<>();
	private final SettingList themeColours = new SettingList(ThemeManager.colourSettings());
	private final SettingList general = new SettingList(ThemeManager.generalSettings());
	private List<String> profiles = new ArrayList<>();

	public MenuScreen() { super(Component.literal("Obsidian Client")); }

	@Override
	protected void init() {
		opened = System.currentTimeMillis();
		search = new EditBox(font, 0, 0, 150, 14, Component.literal("Search"));
		search.setBordered(false);
		search.setHint(Component.literal("Search modules..."));
		search.setResponder(s -> scroll = 0);
		addRenderableWidget(search);
		profiles = Config.profiles();
	}

	// ---- layout ----
	private void layout() {
		pw = Math.min(width - 16, 580);
		ph = Math.min(height - 16, 330);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
	}
	private int cx() { return px + SIDE + 12; }
	private int cw() { return pw - SIDE - 24; }
	private int listTop() { return py + HEADER + 34; }
	private int listBottom() { return py + ph - 22; }

	private List<Module> visible() {
		List<Module> out = new ArrayList<>();
		String q = search == null ? "" : search.getValue().toLowerCase();
		for (Module m : ModuleManager.MODULES) {
			if (cat != null && m.category != cat) continue;
			if (!q.isEmpty() && !m.name.toLowerCase().contains(q) && !m.description.toLowerCase().contains(q)) continue;
			out.add(m);
		}
		return out;
	}

	private int colW() { return (cw() - GAP) / 2; }
	private int rowX(int i) { return cx() + (i % 2) * (colW() + GAP); }
	private int rowY(int i) { return listTop() + (i / 2) * (ROWH + GAP) - (int) scroll; }
	private int maxScroll(int n) { return Math.max(0, ((n + 1) / 2) * (ROWH + GAP) - (listBottom() - listTop())); }

	private int tabX(int i) {
		int x = px + 118;
		for (int k = 0; k < i; k++) x += font.width(Tab.values()[k].label) + 18;
		return x;
	}

	// ---- render ----
	@Override public void renderBackground(GuiGraphics g, int mx, int my, float delta) {}

	@Override
	public void render(GuiGraphics g, int mx, int my, float delta) {
		layout();
		float t = ThemeManager.ANIMATIONS.on() ? Draw.ease((System.currentTimeMillis() - opened) / 240f) : 1f;
		int slide = (int) ((1f - t) * 14);
		g.fill(0, 0, width, height, Draw.a(0xFF000000, 0.55f * t));

		int y0 = py + slide;
		Draw.rect(g, px - 1, y0 - 1, pw + 2, ph + 2, Draw.a(ThemeManager.accent(), 0.35f * t));
		Draw.rect(g, px, y0, pw, ph, Draw.a(ThemeManager.bg(), 0.97f * t));
		g.fillGradient(px + 1, y0 + 1, px + pw - 1, y0 + HEADER, Draw.a(ThemeManager.accent(), 0.18f * t), Draw.a(ThemeManager.accent(), 0f));
		g.fill(px + SIDE, y0 + HEADER, px + SIDE + 1, y0 + ph - 1, Draw.a(ThemeManager.accent(), 0.20f * t));
		g.fill(px + 1, y0 + HEADER, px + pw - 1, y0 + HEADER + 1, Draw.a(ThemeManager.accent(), 0.20f * t));

		// logo + title
		MainMenuOverlay.mark(g, px + 10, y0 + 6, 24, t);
		Draw.text(g, font, "OBSIDIAN", px + 40, y0 + 8, ThemeManager.accent2(), t);
		Draw.text(g, font, "CLIENT", px + 40, y0 + 18, ThemeManager.dim(), t);

		// tabs
		for (int i = 0; i < Tab.values().length; i++) {
			Tab tb = Tab.values()[i];
			int x = tabX(i), w = font.width(tb.label) + 12;
			boolean active = tb == tab, hover = Draw.in(mx, my, x, y0 + 8, w, 20);
			Draw.rect(g, x, y0 + 8, w, 20, Draw.a(active || hover ? ThemeManager.cardHover() : ThemeManager.card(), t));
			if (active) g.fill(x + 2, y0 + 26, x + w - 2, y0 + 28, Draw.a(ThemeManager.accent(), t));
			Draw.text(g, font, tb.label, x + 6, y0 + 14, active ? 0xFFFFFFFF : ThemeManager.dim(), t);
		}
		Draw.text(g, font, "x", px + pw - 14, y0 + 14, Draw.in(mx, my, px + pw - 20, y0 + 6, 16, 22) ? 0xFFFFFFFF : ThemeManager.dim(), t);

		renderSidebar(g, mx, my, y0, t);
		search.visible = tab == Tab.MODULES;
		search.active = tab == Tab.MODULES;

		int oy = py; py = y0; // draw content relative to the sliding panel
		switch (tab) {
			case MODULES -> renderModules(g, mx, my, t);
			case PROFILES -> renderProfiles(g, mx, my, t);
			case THEMES -> renderThemes(g, mx, my, t);
			case ACCOUNT -> renderAccount(g, t);
			case SETTINGS -> renderSettings(g, mx, my, t);
			default -> {}
		}
		py = oy;
		super.render(g, mx, my, delta);
	}

	private void renderSidebar(GuiGraphics g, int mx, int my, int y0, float t) {
		List<String> names = new ArrayList<>();
		names.add("ALL");
		for (Module.Category c : Module.Category.values()) names.add(c.label.toUpperCase());
		for (int i = 0; i < names.size(); i++) {
			Module.Category c = i == 0 ? null : Module.Category.values()[i - 1];
			int by = y0 + HEADER + 10 + i * 26, bx = px + 8, bw = SIDE - 16;
			boolean active = tab == Tab.MODULES && c == cat, hover = Draw.in(mx, my, bx, by, bw, 22);
			Draw.rect(g, bx, by, bw, 22, Draw.a(active || hover ? ThemeManager.cardHover() : ThemeManager.card(), t));
			if (active) g.fill(bx, by + 3, bx + 2, by + 19, Draw.a(ThemeManager.accent(), t));
			Draw.text(g, font, names.get(i), bx + 10, by + 7, active ? 0xFFFFFFFF : ThemeManager.dim(), t);
		}
		long on = ModuleManager.MODULES.stream().filter(Module::isEnabled).count();
		Draw.text(g, font, on + "/" + ModuleManager.MODULES.size() + " enabled", px + 10, y0 + ph - 16, ThemeManager.dim(), t);
	}

	private void renderModules(GuiGraphics g, int mx, int my, float t) {
		int sx = cx();
		Draw.rect(g, sx, py + HEADER + 8, Math.min(190, cw() - 90), 18, Draw.a(ThemeManager.card(), t));
		search.setX(sx + 6); search.setY(py + HEADER + 12); search.setWidth(Math.min(190, cw() - 90) - 12);
		List<Module> mods = visible();
		String count = mods.size() + " MODULES";
		Draw.text(g, font, count, cx() + cw() - font.width(count), py + HEADER + 14, ThemeManager.dim(), t);

		scroll = Math.max(0, Math.min(scroll, maxScroll(mods.size())));
		g.enableScissor(cx() - 2, listTop() - 2, cx() + cw() + 2, listBottom());
		for (int i = 0; i < mods.size(); i++) {
			Module m = mods.get(i);
			int x = rowX(i), y = rowY(i), w = colW();
			if (y + ROWH < listTop() - 4 || y > listBottom()) continue;
			boolean hover = Draw.in(mx, my, x, y, w, ROWH) && my >= listTop() && my < listBottom();
			float target = m.isEnabled() ? 1f : 0f;
			float cur = switches.getOrDefault(m, target);
			cur += (target - cur) * (ThemeManager.ANIMATIONS.on() ? 0.25f : 1f);
			switches.put(m, cur);

			Draw.rect(g, x, y, w, ROWH, Draw.a(hover ? ThemeManager.cardHover() : ThemeManager.card(), t));
			if (m.isEnabled()) g.fill(x, y + 4, x + 2, y + ROWH - 4, Draw.a(ThemeManager.accent(), t));
			int nameCol = m.isEnabled() ? ThemeManager.accent2() : 0xFFE6E1F0;
			Draw.text(g, font, m.name.toUpperCase(), x + 8, y + 5, nameCol, t);
			String d = font.plainSubstrByWidth(m.description, w - 66);
			Draw.text(g, font, d, x + 8, y + 16, ThemeManager.dim(), t);
			SettingList.sw(g, x + w - 32, y + 8, cur, t);
			if (!m.settings.isEmpty()) { // little "..." settings button
				int dx = x + w - 48;
				for (int k = 0; k < 3; k++) g.fill(dx + k * 4, y + 12, dx + k * 4 + 2, y + 14, Draw.a(hover ? 0xFFFFFFFF : ThemeManager.dim(), t));
			}
		}
		g.disableScissor();
		Draw.text(g, font, "Left click: toggle   Right click or ... : settings", cx(), py + ph - 14, ThemeManager.dim(), t);
	}

	private void renderProfiles(GuiGraphics g, int mx, int my, float t) {
		int x = cx(), y = py + HEADER + 12, w = cw();
		Draw.text(g, font, "PROFILES - save and switch whole setups", x, y, ThemeManager.accent2(), t);
		y += 18;
		boolean hover = Draw.in(mx, my, x, y, w, 24);
		Draw.rect(g, x, y, w, 24, Draw.a(hover ? ThemeManager.cardHover() : ThemeManager.card(), t));
		Draw.text(g, font, "+ Save current setup as a new profile", x + 10, y + 8, 0xFFFFFFFF, t);
		y += 30;
		for (String p : profiles) {
			boolean active = p.equals(Config.activeProfile);
			hover = Draw.in(mx, my, x, y, w, 24);
			Draw.rect(g, x, y, w, 24, Draw.a(hover ? ThemeManager.cardHover() : ThemeManager.card(), t));
			if (active) g.fill(x, y + 4, x + 2, y + 20, Draw.a(ThemeManager.accent(), t));
			Draw.text(g, font, p + (active ? "  (active)" : ""), x + 10, y + 8, active ? ThemeManager.accent2() : 0xFFFFFFFF, t);
			Draw.text(g, font, "Save here", x + w - 100, y + 8, ThemeManager.dim(), t);
			Draw.text(g, font, "x", x + w - 14, y + 8, 0xFFEF4444, t);
			y += 28;
			if (y > listBottom()) break;
		}
		if (profiles.isEmpty()) Draw.text(g, font, "No profiles yet.", x, y, ThemeManager.dim(), t);
		Draw.text(g, font, "Click a profile to load it.", x, py + ph - 14, ThemeManager.dim(), t);
	}

	private void renderThemes(GuiGraphics g, int mx, int my, float t) {
		int x = cx(), y = py + HEADER + 12, w = cw();
		Draw.text(g, font, "THEMES", x, y, ThemeManager.accent2(), t);
		y += 16;
		List<String> names = ThemeManager.themeNames();
		int cw = (w - GAP * (names.size() - 1)) / names.size();
		for (int i = 0; i < names.size(); i++) {
			int bx = x + i * (cw + GAP);
			boolean active = names.get(i).equals(ThemeManager.current), hover = Draw.in(mx, my, bx, y, cw, 46);
			Draw.rect(g, bx, y, cw, 46, Draw.a(hover || active ? ThemeManager.cardHover() : ThemeManager.card(), t));
			g.fill(bx + 8, y + 8, bx + cw - 8, y + 20, Draw.a(ThemeManager.swatch(names.get(i)), t));
			Draw.text(g, font, names.get(i), bx + 8, y + 28, active ? 0xFFFFFFFF : ThemeManager.dim(), t);
			if (active) g.fill(bx + 2, y + 44, bx + cw - 2, y + 46, Draw.a(ThemeManager.accent(), t));
		}
		y += 56;
		Draw.text(g, font, "CUSTOM COLOUR (choose the Custom theme)", x, y, ThemeManager.dim(), t);
		themeColours.render(g, x, y + 14, w, mx, my, t);
		general.render(g, x, y + 20 + themeColours.height(), w, mx, my, t);
	}

	private void renderAccount(GuiGraphics g, float t) {
		int x = cx(), y = py + HEADER + 12;
		String name = minecraft.getUser().getName();
		Draw.text(g, font, "OBSIDIAN ACCOUNT", x, y, ThemeManager.accent2(), t);
		String[] lines = {
			"Player:  " + name,
			"Play time with Obsidian:  " + Stats.playtime(),
			"Hits landed:  " + Stats.hits,
			"Best combo:  " + Stats.bestCombo,
			"Total clicks:  " + Stats.clicks,
			"Active profile:  " + (Config.activeProfile.isEmpty() ? "none" : Config.activeProfile),
		};
		y += 18;
		for (String l : lines) {
			Draw.rect(g, x, y, cw(), 20, Draw.a(ThemeManager.card(), t));
			Draw.text(g, font, l, x + 10, y + 6, 0xFFFFFFFF, t);
			y += 24;
		}
		Draw.text(g, font, "Stats are stored on this computer only.", x, y + 4, ThemeManager.dim(), t);
	}

	private void renderSettings(GuiGraphics g, int mx, int my, float t) {
		int x = cx(), y = py + HEADER + 12, w = cw();
		Draw.text(g, font, "SETTINGS", x, y, ThemeManager.accent2(), t);
		general.render(g, x, y + 14, w, mx, my, t);
		y += 20 + general.height();
		Draw.rect(g, x, y, w, 20, Draw.a(ThemeManager.card(), t));
		Draw.text(g, font, UpdateChecker.status, x + 10, y + 6, UpdateChecker.updateAvailable ? 0xFF4ADE80 : 0xFFFFFFFF, t);
		y += 24;
		boolean h1 = Draw.in(mx, my, x, y, 120, 20);
		Draw.rect(g, x, y, 120, 20, Draw.a(h1 ? ThemeManager.cardHover() : ThemeManager.card(), t));
		Draw.text(g, font, "Join our Discord", x + 10, y + 6, 0xFFFFFFFF, t);
		boolean h2 = Draw.in(mx, my, x + 126, y, 120, 20);
		Draw.rect(g, x + 126, y, 120, 20, Draw.a(h2 ? ThemeManager.cardHover() : ThemeManager.card(), t));
		Draw.text(g, font, UpdateChecker.updateAvailable ? "Get the update" : "Releases page", x + 136, y + 6, 0xFFFFFFFF, t);
		y += 28;
		Draw.text(g, font, "Keys: Right Shift menu | C zoom | Alt freelook | V sprint | B sneak (change in Controls)", x, y, ThemeManager.dim(), t);
	}

	// ---- input ----
	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
		layout();
		double mx = e.x(), my = e.y();
		int b = e.button();
		if (Draw.in(mx, my, px + pw - 20, py + 6, 16, 22)) { onClose(); return true; }

		for (int i = 0; i < Tab.values().length; i++) {
			int x = tabX(i), w = font.width(Tab.values()[i].label) + 12;
			if (Draw.in(mx, my, x, py + 8, w, 20)) {
				Tab tb = Tab.values()[i];
				if (tb == Tab.HUD) { minecraft.setScreen(new HudEditorScreen(this)); return true; }
				tab = tb; scroll = 0;
				if (tb == Tab.PROFILES) profiles = Config.profiles();
				return true;
			}
		}
		for (int i = 0; i <= Module.Category.values().length; i++) {
			int by = py + HEADER + 10 + i * 26;
			if (Draw.in(mx, my, px + 8, by, SIDE - 16, 22)) {
				tab = Tab.MODULES; scroll = 0;
				cat = i == 0 ? null : Module.Category.values()[i - 1];
				return true;
			}
		}

		switch (tab) {
			case MODULES -> {
				if (Draw.in(mx, my, cx(), py + HEADER + 8, 190, 18)) return super.mouseClicked(e, dbl);
				List<Module> mods = visible();
				for (int i = 0; i < mods.size(); i++) {
					int x = rowX(i), y = rowY(i);
					if (my < listTop() || my >= listBottom()) continue;
					if (Draw.in(mx, my, x, y, colW(), ROWH)) {
						Module m = mods.get(i);
						boolean dots = !m.settings.isEmpty() && mx >= x + colW() - 54 && mx < x + colW() - 38;
						if (b == 1 || dots) minecraft.setScreen(new ModuleSettingsScreen(this, m));
						else { m.toggle(); Config.save(); }
						return true;
					}
				}
			}
			case PROFILES -> {
				int x = cx(), y = py + HEADER + 30, w = cw();
				if (Draw.in(mx, my, x, y, w, 24)) { Config.newProfile(); profiles = Config.profiles(); return true; }
				y += 30;
				for (String p : new ArrayList<>(profiles)) {
					if (Draw.in(mx, my, x, y, w, 24)) {
						if (mx >= x + w - 24) Config.deleteProfile(p);
						else if (mx >= x + w - 104) Config.overwriteProfile(p);
						else Config.loadProfile(p);
						profiles = Config.profiles();
						return true;
					}
					y += 28;
				}
			}
			case THEMES -> {
				int x = cx(), y = py + HEADER + 28, w = cw();
				List<String> names = ThemeManager.themeNames();
				int cw = (w - GAP * (names.size() - 1)) / names.size();
				for (int i = 0; i < names.size(); i++) {
					if (Draw.in(mx, my, x + i * (cw + GAP), y, cw, 46)) { ThemeManager.current = names.get(i); Config.save(); return true; }
				}
				if (themeColours.click(mx, my, b) || general.click(mx, my, b)) return true;
			}
			case SETTINGS -> {
				if (general.click(mx, my, b)) return true;
				int x = cx(), y = py + HEADER + 12 + 20 + general.height() + 24;
				if (Draw.in(mx, my, x, y, 120, 20)) { Util.getPlatform().openUri(DiscordRpcModule.INVITE); return true; }
				if (Draw.in(mx, my, x + 126, y, 120, 20)) { Util.getPlatform().openUri(UpdateChecker.url); return true; }
			}
			default -> {}
		}
		return super.mouseClicked(e, dbl);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
		if (themeColours.dragging()) { themeColours.drag(e.x()); return true; }
		if (general.dragging()) { general.drag(e.x()); return true; }
		return super.mouseDragged(e, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent e) {
		if (themeColours.dragging() || general.dragging()) Config.save();
		themeColours.release(); general.release();
		return super.mouseReleased(e);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		if (tab == Tab.MODULES) { scroll = Math.max(0, Math.min(maxScroll(visible().size()), scroll - sy * 20)); return true; }
		return super.mouseScrolled(mx, my, sx, sy);
	}

	@Override public void onClose() { Config.save(); super.onClose(); }
	@Override public boolean isPauseScreen() { return false; }
}
