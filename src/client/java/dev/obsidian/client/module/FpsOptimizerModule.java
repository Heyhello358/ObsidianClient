package dev.obsidian.client.module;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;

/** Lowers a few video options for more FPS. Your old values come back when you switch it off or quit. */
public class FpsOptimizerModule extends Module {
	private boolean applied;
	private ParticleStatus particles;
	private double entityDist;
	private int biome;
	private CloudStatus clouds;

	public FpsOptimizerModule() {
		super("fpsoptimizer", "FPS Optimizer", "Lowers entity distance, clouds, biome blend", Category.PERFORMANCE, 0, 0);
	}

	@Override protected void onEnable() {
		Options o = Minecraft.getInstance().options;
		if (applied) return;
		particles = o.particles().get(); entityDist = o.entityDistanceScaling().get();
		biome = o.biomeBlendRadius().get(); clouds = o.cloudStatus().get();
		applied = true;
		o.entityDistanceScaling().set(0.6);
		o.biomeBlendRadius().set(1);
		o.cloudStatus().set(CloudStatus.OFF);
	}
	@Override protected void onDisable() { restore(); }
	@Override public void restore() {
		if (!applied) return;
		Options o = Minecraft.getInstance().options;
		o.entityDistanceScaling().set(entityDist);
		o.biomeBlendRadius().set(biome);
		o.cloudStatus().set(clouds);
		applied = false;
	}
}
