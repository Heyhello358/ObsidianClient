package dev.obsidian.client.module;

import dev.obsidian.client.core.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A bar showing your weapon cooldown. */
public class AttackIndicatorModule extends HudModule {
	public AttackIndicatorModule(int x, int y) {
		super("attackindicator", "Attack Indicator", "Shows your attack cooldown as a bar", Category.PVP, x, y);
	}

	@Override public int width() { return 40; }
	@Override public int height() { return 5; }

	@Override
	public void render(GuiGraphics g) {
		var p = Minecraft.getInstance().player;
		float f = p == null ? 0.6f : p.getAttackStrengthScale(0f);
		g.fill(x, y, x + width(), y + height(), 0xAA000000);
		g.fill(x, y, x + (int) (width() * f), y + height(), f >= 1f ? ThemeManager.accent2() : ThemeManager.accent());
	}
}
