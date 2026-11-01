package dev.obsidian.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import dev.obsidian.client.config.Config;
import dev.obsidian.client.gui.ModuleScreen;
import dev.obsidian.client.module.CpsModule;
import dev.obsidian.client.module.ZoomModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class ObsidianClient implements ClientModInitializer {
	public static final String MOD_ID = "obsidian";

	private static KeyMapping.Category category;
	private static KeyMapping openMenu, zoomKey;
	private static boolean wasAttack, wasUse;

	@Override
	public void onInitializeClient() {
		category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.obsidian.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
		zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.obsidian.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

		// Modules need the Minecraft instance, so build them once the client is ready.
		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
			ModuleManager.init();
			Config.load();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Config.save());

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);

		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
			Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
			(graphics, tickCounter) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.options.hideGui || mc.screen instanceof ModuleScreen) return;
				ModuleManager.renderHud(graphics);
			});
	}

	private void tick(Minecraft mc) {
		while (openMenu.consumeClick()) mc.setScreen(new ModuleScreen());
		ZoomModule.held = zoomKey.isDown() && ModuleManager.get("zoom") != null
			&& ModuleManager.get("zoom").isEnabled();

		// CPS: count presses of the attack/use keys (rising edge).
		boolean attack = mc.options.keyAttack.isDown(), use = mc.options.keyUse.isDown();
		if (attack && !wasAttack) CpsModule.registerClick(0);
		if (use && !wasUse) CpsModule.registerClick(1);
		wasAttack = attack; wasUse = use;

		ModuleManager.tick();
	}
}
