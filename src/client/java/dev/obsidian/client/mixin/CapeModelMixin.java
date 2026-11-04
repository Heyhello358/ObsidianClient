package dev.obsidian.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.obsidian.client.core.Capes;
import dev.obsidian.client.module.NametagModule;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Adds a gentle wave to Obsidian capes (only capes with our logo). */
@Mixin(PlayerCapeModel.class)
public abstract class CapeModelMixin {
	@Shadow @Final private ModelPart cape;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"), require = 0)
	private void obsidian$wave(AvatarRenderState state, CallbackInfo ci) {
		NametagModule m = NametagModule.instance;
		if (m == null || !m.isEnabled() || !m.wavy() || !Capes.isObsidianCape(state.skin)) return;
		float t = state.ageInTicks + state.id * 7f;
		float move = Math.min(1f, state.walkAnimationSpeed);
		cape.xRot += (float) (Math.sin(t * 0.12) * 0.05 + move * Math.sin(t * 0.45) * 0.16);
		cape.zRot += (float) (Math.sin(t * 0.09 + 1.3) * 0.04 + move * Math.sin(t * 0.3) * 0.07);
	}
}
