package dev.obsidian.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.obsidian.client.core.Nametags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public class EntityNameMixin {
	// require = 0: if this changes in a future version, logos are skipped instead of crashing.
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
	private void obsidian$tag(CallbackInfoReturnable<Component> cir) {
		if (!((Object) this instanceof Player p)) return;
		Component out = Nametags.decorate(p.getUUID(), cir.getReturnValue());
		if (out != null) cir.setReturnValue(out);
	}
}
