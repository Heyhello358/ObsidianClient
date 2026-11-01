package dev.obsidian.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;

/** Shows entity hitboxes (the same as vanilla F3+B). */
public class HitboxesModule extends Module {
	private DebugScreenEntryStatus old;

	public HitboxesModule() {
		super("hitboxes", "Hitboxes", "Shows entity hitboxes like F3+B", Category.GAMEPLAY, 0, 0);
	}

	@Override protected void onEnable() {
		var list = Minecraft.getInstance().debugEntries;
		if (old == null) old = list.getStatus(DebugScreenEntries.ENTITY_HITBOXES);
		list.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
	}
	@Override protected void onDisable() { restore(); }
	@Override public void restore() {
		if (old == null) return;
		Minecraft.getInstance().debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, old);
		old = null;
	}
}
