package dev.obsidian.client.module;

import net.minecraft.client.Minecraft;

/** Shows entity hitboxes (the same as vanilla F3+B). */
public class HitboxesModule extends Module {
	public HitboxesModule() {
		super("hitboxes", "Hitboxes", "Shows entity hitboxes like F3+B", Category.GAMEPLAY, 0, 0);
	}
	@Override protected void onEnable() { Minecraft.getInstance().getEntityRenderDispatcher().setRenderHitBoxes(true); }
	@Override protected void onDisable() { Minecraft.getInstance().getEntityRenderDispatcher().setRenderHitBoxes(false); }
	@Override public void restore() { Minecraft.getInstance().getEntityRenderDispatcher().setRenderHitBoxes(false); }
}
