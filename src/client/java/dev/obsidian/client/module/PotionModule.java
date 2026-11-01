package dev.obsidian.client.module;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

/** Active potion effects with time left. */
public class PotionModule extends HudModule {
	public PotionModule(int x, int y) {
		super("potions", "Potion Effects", "Lists your active effects and their timers", Category.GAMEPLAY, x, y);
	}

	private List<String> lines() {
		List<String> out = new ArrayList<>();
		var p = Minecraft.getInstance().player;
		if (p != null) {
			for (MobEffectInstance e : p.getActiveEffects()) {
				String name = e.getEffect().value().getDisplayName().getString();
				if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
				String time = e.isInfiniteDuration() ? "**" : String.format("%d:%02d", e.getDuration() / 1200, (e.getDuration() / 20) % 60);
				out.add(name + "  " + time);
			}
		}
		if (out.isEmpty() && editing()) out.add("Speed II  1:30");
		return out;
	}

	@Override public int width() {
		int w = 60;
		for (String s : lines()) w = Math.max(w, Minecraft.getInstance().font.width(s) + 10);
		return w;
	}
	@Override public int height() { return Math.max(1, lines().size()) * 12 + 4; }

	@Override
	public void render(GuiGraphics g) {
		List<String> l = lines();
		if (l.isEmpty()) return;
		box(g, width(), height());
		int ry = y + 3;
		for (String s : l) { text(g, s, x + 5, ry, color()); ry += 12; }
	}
}
