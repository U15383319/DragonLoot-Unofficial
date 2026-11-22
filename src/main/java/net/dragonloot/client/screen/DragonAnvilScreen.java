package net.dragonloot.client.screen;

import net.dragonloot.menu.DragonAnvilMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

public final class DragonAnvilScreen extends AnvilScreen {
    public DragonAnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int cost = menu.getCost();
        var result = menu.getSlot(menu.getResultSlot());
        if (cost < DragonAnvilMenu.VANILLA_COST_LIMIT || !result.hasItem()) {
            super.renderLabels(graphics, mouseX, mouseY);
            return;
        }

        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
        Component label = Component.translatable("container.repair.cost", cost);
        int color = result.mayPickup(minecraft.player) ? 8453920 : 16736352;
        int left = imageWidth - 8 - font.width(label) - 2;
        graphics.fill(left - 2, 67, imageWidth - 8, 79, 1325400064);
        graphics.drawString(font, label, left, 69, color);
    }
}
