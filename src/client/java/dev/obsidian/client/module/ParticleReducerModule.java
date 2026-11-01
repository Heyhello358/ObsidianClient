package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

/** Fewer particles for more FPS. Restores your setting when off. */
public class ParticleReducerModule extends Module {
	private final Setting level = mode("level", "Level", "Decreased", "Minimal");
	private boolean applied;
	private ParticleStatus old;

	public ParticleReducerModule() {
		super("particles", "Particle Reducer", "Cuts particle count to save FPS", Category.PERFORMANCE, 0, 0);
	}

	@Override protected void onEnable() {
		var o = Minecraft.getInstance().options;
		if (!applied) { old = o.particles().get(); applied = true; }
		o.particles().set(level.intValue() == 1 ? ParticleStatus.MINIMAL : ParticleStatus.DECREASED);
	}
	@Override public void tick() {
		var o = Minecraft.getInstance().options;
		ParticleStatus want = level.intValue() == 1 ? ParticleStatus.MINIMAL : ParticleStatus.DECREASED;
		if (applied && o.particles().get() != want) o.particles().set(want);
	}
	@Override protected void onDisable() { restore(); }
	@Override public void restore() {
		if (!applied) return;
		Minecraft.getInstance().options.particles().set(old);
		applied = false;
	}
}
