package dev.obsidian.client.module;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A HUD box with one line of text. */
public class TextModule extends HudModule {
	private final Supplier<String> text;

	public TextModule(String id, String name, String description, Category cat, int x, int y, Supplier<String> text) {
		super(id, name, description, cat, x, y);
		this.text = text;
	}

	@Override public int width() { return Minecraft.getInstance().font.width(text.get()) + 10; }
	@Override public int height() { return 14; }

	@Override
	public void render(GuiGraphics g) {
		box(g, width(), height());
		text(g, text.get(), x + 5, y + 3, color());
	}
}
