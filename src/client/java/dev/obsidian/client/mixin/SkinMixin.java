package dev.obsidian.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.obsidian.client.core.Capes;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;

@Mixin(AbstractClientPlayer.class)
public abstract class SkinMixin {
	// require = 0: if this changes in a future version, capes are skipped instead of crashing.
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, require = 0)
	private void obsidian$cape(CallbackInfoReturnable<PlayerSkin> cir) {
		PlayerSkin out = Capes.apply(((AbstractClientPlayer) (Object) this).getUUID(), cir.getReturnValue());
		if (out != null) cir.setReturnValue(out);
	}
}
