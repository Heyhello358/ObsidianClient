package dev.obsidian.client.module;

/** Hold the zoom key to narrow the field of view. Does nothing else. */
public class ZoomModule extends Module {
	public static boolean held;
	public static double factor = 0.25;

	public ZoomModule() {
		super("zoom", "Zoom", "Hold C to zoom in, scroll to adjust", Category.PLAYER, 0, 0);
		setEnabled(true);
	}

	/** Applied by the FOV mixin. */
	public static float apply(float fov) {
		return held ? (float) (fov * factor) : fov;
	}
}
