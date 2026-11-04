package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;

/** Obsidian network: shows role logos on other Obsidian players and lets them see yours. */
public class NametagModule extends Module {
	public static NametagModule instance;
	private final Setting share = bool("share", "Show my logo to others", true);
	private final Setting others = bool("others", "Show others' logos", true);

	public NametagModule() {
		super("nametags", "Obsidian Nametags", "Role logo and colour on Obsidian players' nametags", Category.CLIENT, 0, 0);
		instance = this;
	}
	public boolean share() { return share.on(); }
	public boolean showOthers() { return others.on(); }
}
