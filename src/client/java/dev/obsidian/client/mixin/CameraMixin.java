package dev.obsidian.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.obsidian.client.module.FreelookModule;
import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow protected abstract void setRotation(float yRot, float xRot);

	// require = 0: if this method changes, freelook is skipped instead of crashing.
	@Inject(method = "setup", at = @At("TAIL"), require = 0)
	private void obsidian$freelook(CallbackInfo ci) {
		if (FreelookModule.active) setRotation(FreelookModule.camYaw, FreelookModule.camPitch);
	}
}
