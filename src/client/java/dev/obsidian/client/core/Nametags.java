package dev.obsidian.client.core;

import java.util.UUID;

import dev.obsidian.client.module.NametagModule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Builds the decorated nametag: [logo] Name, tinted with the role / custom colour. */
public final class Nametags {
	private Nametags() {}

	/** Returns the new display name, or null to leave the original. */
	public static Component decorate(UUID id, Component original) {
		NametagModule m = NametagModule.instance;
		if (m == null || !m.isEnabled() || !m.showOthers()) return null;
		ObsidianNet.Tag t = ObsidianNet.tag(id);
		if (t == null) return null;
		int logoColor = t.color() >= 0 ? t.color() : t.role().color;
		int nameColor = t.color() >= 0 ? t.color() : (t.role() == Role.MEMBER ? -1 : t.role().color);
		MutableComponent icon = Component.literal(String.valueOf(t.role().glyph))
			.withStyle(Style.EMPTY.withColor(logoColor));
		MutableComponent name = original.copy();
		if (nameColor >= 0) name = name.withStyle(Style.EMPTY.withColor(nameColor));
		return Component.empty().append(icon).append(Component.literal(" ")).append(name);
	}
}
