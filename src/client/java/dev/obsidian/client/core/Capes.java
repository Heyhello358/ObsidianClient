package dev.obsidian.client.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.obsidian.client.module.NametagModule;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/** Gives Obsidian players the cape that matches their role. */
public final class Capes {
	private static final Map<Role, ClientAsset.ResourceTexture> CACHE = new EnumMap<>(Role.class);

	private Capes() {}

	private static ClientAsset.ResourceTexture texture(Role r) {
		return CACHE.computeIfAbsent(r, k -> new ClientAsset.ResourceTexture(
			Identifier.fromNamespaceAndPath("obsidian", "cape/" + k.id),
			Identifier.fromNamespaceAndPath("obsidian", "textures/cape/" + k.id + ".png")));
	}

	/** Returns a skin with the role cape, or null to leave the skin alone. */
	public static PlayerSkin apply(UUID id, PlayerSkin skin) {
		NametagModule m = NametagModule.instance;
		if (m == null || !m.isEnabled() || !m.capes()) return null;
		ObsidianNet.Tag t = ObsidianNet.tag(id);
		if (t == null) return null;
		if (!m.replaceCapes() && skin.cape() != null) return null;
		return skin.with(new PlayerSkin.Patch(Optional.empty(), Optional.of(texture(t.role())), Optional.empty(), Optional.empty()));
	}

	public static boolean isObsidianCape(PlayerSkin skin) {
		return skin != null && skin.cape() != null && "obsidian".equals(skin.cape().id().getNamespace());
	}
}
