package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Sneak without holding the key. Press the toggle key (default B) to switch. */
public class ToggleSneakModule extends HudModule {
	public static boolean active;
	private final Setting status = bool("status", "Show status", true);

	public ToggleSneakModule(int x, int y) {
		super("togglesneak", "Toggle Sneak", "Sneak without holding the key (key: B)", Category.GAMEPLAY, x, y);
	}

	@Override public int width() { return Minecraft.getInstance().font.width(label()) + 10; }
	@Override public int height() { return 14; }
	private String label() { return active ? "[Sneak: Toggled]" : "[Sneak: Off]"; }

	@Override public void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.screen == null && active) mc.options.keyShift.setDown(true);
	}
	@Override protected void onDisable() {
		active = false;
		Minecraft.getInstance().options.keyShift.setDown(false);
	}

	public static void flip() {
		active = !active;
		if (!active) Minecraft.getInstance().options.keyShift.setDown(false);
	}

	@Override
	public void render(GuiGraphics g) {
		if (!status.on()) return;
		box(g, width(), height());
		text(g, label(), x + 5, y + 3, color());
	}
}
