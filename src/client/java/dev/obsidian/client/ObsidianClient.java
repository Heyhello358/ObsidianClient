package dev.obsidian.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import dev.obsidian.client.config.Config;
import dev.obsidian.client.core.CombatState;
import dev.obsidian.client.core.ObsidianNet;
import dev.obsidian.client.core.Stats;
import dev.obsidian.client.core.UpdateChecker;
import dev.obsidian.client.gui.ObsidianTitleScreen;
import dev.obsidian.client.gui.MenuScreen;
import dev.obsidian.client.module.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

public class ObsidianClient implements ClientModInitializer {
	public static final String MOD_ID = "obsidian";

	private static KeyMapping.Category category;
	private static KeyMapping openMenu, zoomKey, freelookKey, sprintKey, sneakKey;
	private static boolean wasAttack, wasUse;
	private static int ticks;

	private static KeyMapping key(String name, int code) {
		return KeyBindingHelper.registerKeyBinding(new KeyMapping(name, InputConstants.Type.KEYSYM, code, category));
	}

	@Override
	public void onInitializeClient() {
		category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openMenu = key("key.obsidian.menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
		zoomKey = key("key.obsidian.zoom", GLFW.GLFW_KEY_C);
		freelookKey = key("key.obsidian.freelook", GLFW.GLFW_KEY_LEFT_ALT);
		sprintKey = key("key.obsidian.togglesprint", GLFW.GLFW_KEY_V);
		sneakKey = key("key.obsidian.togglesneak", GLFW.GLFW_KEY_B);

		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
			ModuleManager.init();
			Config.load();
			UpdateChecker.checkAsync();
			ObsidianNet.start();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
			Config.save();
			ModuleManager.restoreOptions(); // put changed video options back before the game saves them
		});

		ClientTickEvents.START_CLIENT_TICK.register(FreelookModule::update);
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);

		// Display-only: we just remember that you attacked, we never change the attack.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClientSide()) CombatState.onAttack(entity);
			return InteractionResult.PASS;
		});

		// HUD
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
			(graphics, tickCounter) -> {
				Minecraft mc = Minecraft.getInstance();
				FreelookModule.update(mc);
				if (mc.options.hideGui || mc.screen instanceof MenuScreen) return;
				ModuleManager.renderHud(graphics);
			});
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, orig -> (g, tc) -> {
			CrosshairModule c = CrosshairModule.instance;
			if (c != null && c.isEnabled()) c.draw(g); else orig.render(g, tc);
		});
		HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, orig -> (g, tc) -> {
			ScoreboardModule s = ScoreboardModule.instance;
			if (s == null || !s.isEnabled()) { orig.render(g, tc); return; }
			if (s.hide.on()) return;
			float sc = (float) s.scale.num();
			g.pose().pushMatrix();
			g.pose().translate(g.guiWidth(), g.guiHeight() / 2f);
			g.pose().scale(sc, sc);
			g.pose().translate(-g.guiWidth(), -g.guiHeight() / 2f);
			orig.render(g, tc);
			g.pose().popMatrix();
		});
	}

	private void tick(Minecraft mc) {
		// Swap the vanilla title screen for ours (when "Custom Main Menu" is on).
		var mm = ModuleManager.get("mainmenu");
		if (mm != null && mm.isEnabled() && mc.screen != null && mc.screen.getClass() == TitleScreen.class)
			mc.setScreen(new ObsidianTitleScreen());
		while (openMenu.consumeClick()) mc.setScreen(new MenuScreen());
		ZoomModule.held = zoomKey.isDown();
		FreelookModule.keyHeld = freelookKey.isDown();
		while (sprintKey.consumeClick()) ToggleSprintModule.active = !ToggleSprintModule.active;
		while (sneakKey.consumeClick()) ToggleSneakModule.flip();

		boolean attack = mc.options.keyAttack.isDown(), use = mc.options.keyUse.isDown();
		if (attack && !wasAttack) CpsModule.registerClick(0);
		if (use && !wasUse) CpsModule.registerClick(1);
		wasAttack = attack; wasUse = use;

		if (mc.level != null && ++ticks % 20 == 0) Stats.playSeconds++;
		if (ticks % 60 == 0) ObsidianNet.tick(mc);
		CombatState.tick(mc);
		ModuleManager.tick();
	}
}
