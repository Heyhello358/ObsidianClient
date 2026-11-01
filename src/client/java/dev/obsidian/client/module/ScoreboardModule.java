package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;

/** Scoreboard customizer: hide it or scale it. Applied in the HUD element wrapper. */
public class ScoreboardModule extends Module {
	public static ScoreboardModule instance;
	public final Setting hide = bool("hide", "Hide scoreboard", false);
	public final Setting scale = num("scale", "Scale", 1.0, 0.5, 1.5, 0.1);

	public ScoreboardModule() {
		super("scoreboard", "Scoreboard", "Hide or resize the sidebar scoreboard", Category.VISUALS, 0, 0);
		instance = this;
	}
}
