package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/** Keeps you sprinting while you walk forward. Press the toggle key to turn it on/off. */
public class ToggleSprintModule extends HudModule {
	public static boolean active = true;
	private final Setting status = bool("status", "Show status", true);

	public ToggleSprintModule(int x, int y) {
		super("togglesprint", "Toggle Sprint", "Sprint automatically while walking forward (key: V)", Category.GAMEPLAY, x, y);
	}

	@Override public int width() { return Minecraft.getInstance().font.width(label()) + 10; }
	@Override public int height() { return 14; }

	private String label() { return active ? "[Sprint: Toggled]" : "[Sprint: Off]"; }

	@Override
	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !active || mc.screen != null) return;
		if (mc.options.keyUp.isDown() && !p.isUsingItem() && !p.horizontalCollision && !p.isCrouching()
				&& p.getFoodData().getFoodLevel() > 6) {
			p.setSprinting(true);
		}
	}

	@Override
	public void render(GuiGraphics g) {
		if (!status.on()) return;
		box(g, width(), height());
		text(g, label(), x + 5, y + 3, color());
	}
}
