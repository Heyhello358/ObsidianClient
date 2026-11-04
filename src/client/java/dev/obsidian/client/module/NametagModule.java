package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;

/** Obsidian network: shows role logos on other Obsidian players and lets them see yours. */
public class NametagModule extends Module {
	public static NametagModule instance;
	private final Setting share = bool("share", "Show my logo to others", true);
	private final Setting others = bool("others", "Show others' logos", true);
	private final Setting capes = bool("capes", "Obsidian capes", true);
	private final Setting wavy = bool("wavy", "Wavy capes", true);
	private final Setting replace = bool("replace", "Replace other capes", true);

	public NametagModule() {
		super("nametags", "Obsidian Nametags", "Role logos, colours and capes for Obsidian players", Category.CLIENT, 0, 0);
		instance = this;
	}
	public boolean share() { return share.on(); }
	public boolean showOthers() { return others.on(); }
	public boolean capes() { return capes.on(); }
	public boolean wavy() { return wavy.on(); }
	public boolean replaceCapes() { return replace.on(); }
}
