package dev.obsidian.client.module;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Options;

/** WASD + jump + mouse display. */
public class KeystrokesModule extends Module {
	private static final int SIZE = 20, GAP = 2;

	public KeystrokesModule(int x, int y) {
		super("keystrokes", "Keystrokes", "Shows your movement keys", Category.HUD, x, y);
	}

	@Override public boolean isHud() { return true; }
	@Override public int width() { return SIZE * 3 + GAP * 2; }
	@Override public int height() { return SIZE * 3 + GAP * 2 + 6; }

	@Override
	public void render(GuiGraphics g) {
		Options o = Minecraft.getInstance().options;
		int col = SIZE + GAP;
		key(g, o.keyUp, "W", x + col, y);
		key(g, o.keyLeft, "A", x, y + col);
		key(g, o.keyDown, "S", x + col, y + col);
		key(g, o.keyRight, "D", x + col * 2, y + col);
		// jump bar
		int by = y + col * 2;
		boolean jump = o.keyJump.isDown();
		g.fill(x, by, x + width(), by + 10, jump ? 0xB0FFFFFF : 0x90000000);
		g.fill(x + 6, by + 4, x + width() - 6, by + 6, jump ? 0xFF000000 : 0xFFFFFFFF);
	}

	private void key(GuiGraphics g, KeyMapping k, String label, int kx, int ky) {
		boolean down = k.isDown();
		g.fill(kx, ky, kx + SIZE, ky + SIZE, down ? 0xB0FFFFFF : 0x90000000);
		var font = Minecraft.getInstance().font;
		g.drawString(font, label, kx + (SIZE - font.width(label)) / 2, ky + 6,
			down ? 0xFF000000 : 0xFFFFFFFF, false);
	}
}
