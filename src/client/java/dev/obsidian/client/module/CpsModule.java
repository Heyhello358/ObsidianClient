package dev.obsidian.client.module;

import java.util.ArrayDeque;

import dev.obsidian.client.core.Stats;

/** Clicks-per-second counter. Purely informational. */
public class CpsModule extends TextModule {
	private static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();

	public CpsModule(int x, int y) {
		super("cps", "CPS Counter", "Shows your clicks per second", Category.GAMEPLAY, x, y,
			() -> count(LEFT) + " | " + count(RIGHT) + " CPS");
	}

	public static void registerClick(int button) {
		long now = System.currentTimeMillis();
		if (button == 0) LEFT.add(now); else RIGHT.add(now);
		Stats.clicks++;
	}
	public static int left() { return count(LEFT); }
	public static int right() { return count(RIGHT); }

	private static int count(ArrayDeque<Long> q) {
		long cutoff = System.currentTimeMillis() - 1000;
		while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
		return q.size();
	}
}
