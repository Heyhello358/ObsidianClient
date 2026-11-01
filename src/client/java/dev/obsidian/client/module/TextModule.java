package dev.obsidian.client.module;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A simple HUD box showing one line of text. */
public class TextModule extends Module {
	private final Supplier<String> text;

	public TextModule(String id, String name, String description, int x, int y, Supplier<String> text) {
		super(id, name, description, Category.HUD, x, y);
		this.text = text;
	}

	@Override public boolean isHud() { return true; }

	@Override public int width() {
		return Minecraft.getInstance().font.width(text.get()) + 8;
	}
	@Override public int height() { return 14; }

	@Override
	public void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		g.fill(x, y, x + width(), y + height(), 0x90000000);
		g.drawString(mc.font, text.get(), x + 4, y + 3, 0xFFFFFFFF, true);
	}
}
