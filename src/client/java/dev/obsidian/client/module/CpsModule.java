package dev.obsidian.client.module;

import java.util.ArrayDeque;

import net.minecraft.client.Minecraft;

/** Clicks-per-second counter. Purely informational. */
public class CpsModule extends TextModule {
	private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
	private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();

	public CpsModule(int x, int y) {
		super("cps", "CPS", "Shows your clicks per second", x, y,
			() -> count(LEFT) + " | " + count(RIGHT) + " CPS");
	}

	public static void registerClick(int button) {
		long now = System.currentTimeMillis();
		if (button == 0) LEFT.add(now);
		else if (button == 1) RIGHT.add(now);
	}

	private static int count(ArrayDeque<Long> q) {
		long cutoff = System.currentTimeMillis() - 1000;
		while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
		return q.size();
	}
}
