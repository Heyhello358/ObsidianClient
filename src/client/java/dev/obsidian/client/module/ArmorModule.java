package dev.obsidian.client.module;

import dev.obsidian.client.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Worn armour and held item with durability. */
public class ArmorModule extends HudModule {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
	};
	private final Setting percent = bool("percent", "Show percent", false);

	public ArmorModule(int x, int y) {
		super("armor", "Armor Status", "Shows armor and held item durability", Category.GAMEPLAY, x, y);
	}

	@Override public int width() { return 54; }
	@Override public int height() { return SLOTS.length * 18; }

	@Override
	public void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		box(g, width(), height());
		int row = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack s = mc.player.getItemBySlot(slot);
			int ry = y + row++ * 18;
			if (s.isEmpty()) continue;
			g.renderItem(s, x + 1, ry + 1);
			if (s.isDamageableItem()) {
				int left = s.getMaxDamage() - s.getDamageValue();
				String t = percent.on() ? (left * 100 / s.getMaxDamage()) + "%" : Integer.toString(left);
				text(g, t, x + 20, ry + 5, color());
			}
		}
	}
}
