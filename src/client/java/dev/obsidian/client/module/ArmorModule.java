package dev.obsidian.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Shows worn armor and held item with durability. */
public class ArmorModule extends Module {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
	};

	public ArmorModule(int x, int y) {
		super("armor", "Armor Status", "Shows armor and held item durability", Category.HUD, x, y);
	}

	@Override public boolean isHud() { return true; }
	@Override public int width() { return 52; }
	@Override public int height() { return SLOTS.length * 18; }

	@Override
	public void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		int row = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack s = mc.player.getItemBySlot(slot);
			int ry = y + row++ * 18;
			if (s.isEmpty()) continue;
			g.renderItem(s, x, ry);
			if (s.isDamageableItem()) {
				int left = s.getMaxDamage() - s.getDamageValue();
				g.drawString(mc.font, Integer.toString(left), x + 20, ry + 5, 0xFFFFFFFF, true);
			}
		}
	}
}
