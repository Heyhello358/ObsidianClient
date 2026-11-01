package dev.obsidian.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.obsidian.client.module.ZoomModule;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	// require = 0: if the method signature differs, zoom is skipped instead of crashing the game.
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
	private void obsidian$zoom(net.minecraft.client.Camera camera, float partialTick, boolean useFovSetting,
			CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(ZoomModule.apply(cir.getReturnValueF()));
	}
}
