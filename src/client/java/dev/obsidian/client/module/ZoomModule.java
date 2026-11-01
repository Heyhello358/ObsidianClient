package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;

/** Hold the zoom key to narrow your field of view. Does nothing else. */
public class ZoomModule extends Module {
	public static ZoomModule instance;
	public static boolean held;
	private final Setting level = num("level", "Zoom level", 4, 1.5, 10, 0.5);

	public ZoomModule() {
		super("zoom", "Zoom", "Hold C to zoom in", Category.GAMEPLAY, 0, 0);
		instance = this;
	}

	public static float apply(float fov) {
		return held && instance != null && instance.isEnabled() ? (float) (fov / instance.level.num()) : fov;
	}
}
