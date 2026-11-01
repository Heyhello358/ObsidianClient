package dev.obsidian.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** Hold the key to look around without turning your character. Camera only. */
public class FreelookModule extends Module {
	public static FreelookModule instance;
	public static boolean keyHeld, active;
	public static float camYaw, camPitch, frozenYaw, frozenPitch;

	public FreelookModule() {
		super("freelook", "Freelook", "Hold ALT to look around without turning", Category.GAMEPLAY, 0, 0);
		instance = this;
	}

	/** Called every frame and every tick. */
	public static void update(Minecraft mc) {
		LocalPlayer p = mc.player;
		boolean want = keyHeld && instance != null && instance.isEnabled() && p != null && mc.screen == null;
		if (want && !active) {
			active = true;
			frozenYaw = p.getYRot(); frozenPitch = p.getXRot();
			camYaw = frozenYaw; camPitch = frozenPitch;
		} else if (!want && active) {
			active = false;
		}
		if (active && p != null) {
			camYaw += p.getYRot() - frozenYaw;
			camPitch = Mth.clamp(camPitch + (p.getXRot() - frozenPitch), -90f, 90f);
			p.setYRot(frozenYaw); p.setXRot(frozenPitch);
			p.yRotO = frozenYaw; p.xRotO = frozenPitch;
		}
	}
}
