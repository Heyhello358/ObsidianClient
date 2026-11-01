package dev.obsidian.client.core;

/** Local "Obsidian Account" statistics, saved with the config. */
public final class Stats {
	public static long playSeconds, hits, clicks, bestCombo;
	private Stats() {}

	public static String playtime() {
		long h = playSeconds / 3600, m = (playSeconds / 60) % 60;
		return h + "h " + m + "m";
	}
}
