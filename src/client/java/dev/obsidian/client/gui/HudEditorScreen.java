package dev.obsidian.client.gui;

import dev.obsidian.client.ModuleManager;
import dev.obsidian.client.config.Config;
import dev.obsidian.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Drag enabled HUD modules around. Esc saves and goes back. */
public class HudEditorScreen extends Screen {
	private final Screen parent;
	private Module dragging;
	private double offX, offY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("Edit HUD"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
		g.fill(0, 0, width, height, 0x60000000);
		g.drawCenteredString(font, "Drag modules to move them - Esc to save", width / 2, 8, 0xFFFFFFFF);
		for (Module m : ModuleManager.MODULES) {
			if (!m.isEnabled() || !m.isHud()) continue;
			m.render(g);
			int c = m == dragging ? 0xFF8B5CF6 : 0x80FFFFFF;
			g.renderOutline(m.x - 1, m.y - 1, m.width() + 2, m.height() + 2, c);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
		for (Module m : ModuleManager.MODULES) {
			if (!m.isEnabled() || !m.isHud()) continue;
			if (e.x() >= m.x && e.x() < m.x + m.width() && e.y() >= m.y && e.y() < m.y + m.height()) {
				dragging = m; offX = e.x() - m.x; offY = e.y() - m.y;
				return true;
			}
		}
		return super.mouseClicked(e, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
		if (dragging == null) return false;
		dragging.x = (int) Math.max(0, Math.min(width - dragging.width(), e.x() - offX));
		dragging.y = (int) Math.max(0, Math.min(height - dragging.height(), e.y() - offY));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent e) {
		dragging = null;
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 256) { onClose(); return true; } // Esc
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		Config.save();
		minecraft.setScreen(parent);
	}

	@Override public boolean isPauseScreen() { return false; }
}
