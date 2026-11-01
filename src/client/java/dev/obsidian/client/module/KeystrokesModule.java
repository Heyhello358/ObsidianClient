package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import dev.obsidian.client.core.ThemeManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;

/** WASD, mouse buttons and jump bar. */
public class KeystrokesModule extends HudModule {
	private static final int S = 20, GAP = 2;
	private final Setting mouse = bool("mouse", "Show mouse + CPS", true);

	public KeystrokesModule(int x, int y) {
		super("keystrokes", "Keystrokes", "Shows your movement keys and mouse buttons", Category.GAMEPLAY, x, y);
	}

	@Override public int width() { return S * 3 + GAP * 2; }
	@Override public int height() { return S * 2 + GAP * 2 + 10 + (mouse.on() ? S + GAP : 0); }

	@Override
	public void render(GuiGraphics g) {
		Options o = Minecraft.getInstance().options;
		int c = S + GAP;
		key(g, o.keyUp, "W", x + c, y, S, S);
		key(g, o.keyLeft, "A", x, y + c, S, S);
		key(g, o.keyDown, "S", x + c, y + c, S, S);
		key(g, o.keyRight, "D", x + c * 2, y + c, S, S);
		int by = y + c * 2;
		if (mouse.on()) {
			int hw = (width() - GAP) / 2;
			key(g, o.keyAttack, CpsModule.left() + " CPS", x, by, hw, S);
			key(g, o.keyUse, CpsModule.right() + " CPS", x + hw + GAP, by, hw, S);
			by += c;
		}
		boolean jump = o.keyJump.isDown();
		g.fill(x, by, x + width(), by + 10, jump ? ThemeManager.accent() : ThemeManager.hudBg());
		g.fill(x + 6, by + 4, x + width() - 6, by + 6, jump ? 0xFFFFFFFF : 0xFFAAAAAA);
	}

	private void key(GuiGraphics g, KeyMapping k, String label, int kx, int ky, int w, int h) {
		boolean down = k.isDown();
		g.fill(kx, ky, kx + w, ky + h, down ? ThemeManager.accent() : ThemeManager.hudBg());
		var font = Minecraft.getInstance().font;
		g.drawString(font, label, kx + (w - font.width(label)) / 2, ky + (h - 8) / 2, 0xFFFFFFFF, false);
	}
}
