package dev.obsidian.client.module;

import dev.obsidian.client.core.CombatState;
import dev.obsidian.client.core.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;

/** Shows the name and health of the last mob or player you hit. */
public class TargetHudModule extends HudModule {
	public TargetHudModule(int x, int y) {
		super("targethud", "Target HUD", "Name and health of whoever you last hit", Category.PVP, x, y);
	}

	@Override public int width() { return 120; }
	@Override public int height() { return 30; }

	@Override
	public void render(GuiGraphics g) {
		LivingEntity t = CombatState.target;
		boolean fresh = t != null && System.currentTimeMillis() - CombatState.lastHitTime < 5000;
		if (!fresh && !editing()) return;
		String name = fresh ? t.getName().getString() : "Target";
		float hp = fresh ? t.getHealth() : 14, max = fresh ? t.getMaxHealth() : 20;
		box(g, width(), height());
		text(g, name, x + 6, y + 5, color());
		String s = String.format("%.1f / %.0f", hp, max);
		var font = Minecraft.getInstance().font;
		text(g, s, x + width() - 6 - font.width(s), y + 5, 0xFFAAAAAA);
		int bx = x + 6, bw = width() - 12, by = y + 19;
		g.fill(bx, by, bx + bw, by + 5, 0xFF222222);
		float f = Math.max(0f, Math.min(1f, hp / Math.max(1f, max)));
		g.fill(bx, by, bx + (int) (bw * f), by + 5, f > 0.5f ? ThemeManager.accent() : (f > 0.25f ? 0xFFF59E0B : 0xFFEF4444));
	}
}
