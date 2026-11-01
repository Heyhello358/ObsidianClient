package dev.obsidian.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Display-only combat statistics (combo, reach, w-taps, damage). It never changes what you do. */
public final class CombatState {
	public static int combo, wtaps;
	public static double lastReach;
	public static long lastHitTime, lastHitInterval;
	public static double lastDamage;
	public static LivingEntity target;

	private static float healthAtHit;
	private static int damageTicks;
	private static boolean wasSprinting, pendingTap;

	private CombatState() {}

	public static void onAttack(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !(entity instanceof LivingEntity le) || !le.isAlive()) return;
		long now = System.currentTimeMillis();
		lastHitInterval = lastHitTime == 0 ? 0 : now - lastHitTime;
		lastHitTime = now;
		target = le;
		combo++;
		Stats.hits++;
		if (combo > Stats.bestCombo) Stats.bestCombo = combo;

		Vec3 eye = p.getEyePosition();
		AABB box = le.getBoundingBox();
		Vec3 end = eye.add(p.getLookAngle().scale(8));
		lastReach = box.clip(eye, end).map(eye::distanceTo).orElse(eye.distanceTo(le.position()));

		healthAtHit = le.getHealth();
		damageTicks = 12;
	}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (p == null) return;
		if (p.hurtTime == p.hurtDuration - 1 && p.hurtDuration > 0) combo = 0; // you were hit
		if (target != null && !target.isAlive() && System.currentTimeMillis() - lastHitTime > 3000) target = null;

		if (damageTicks > 0 && target != null) {
			damageTicks--;
			double d = healthAtHit - target.getHealth();
			if (d > 0.05) { lastDamage = d; damageTicks = 0; }
		}

		boolean sprint = p.isSprinting();
		long since = System.currentTimeMillis() - lastHitTime;
		if (wasSprinting && !sprint && since < 600) pendingTap = true;
		if (!wasSprinting && sprint && pendingTap && since < 900) { wtaps++; pendingTap = false; }
		if (since > 900) pendingTap = false;
		wasSprinting = sprint;
	}
}
