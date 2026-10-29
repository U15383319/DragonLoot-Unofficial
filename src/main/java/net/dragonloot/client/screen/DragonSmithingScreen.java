package net.dragonloot.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.PlayerInventory;

public class DragonSmithingScreen extends ContainerScreen<DragonSmithingMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/smithing.png");

    public DragonSmithingScreen(DragonSmithingMenu menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.titleLabelX = 44;
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        fill(poseStack, leftPos + 24, topPos + 44, leftPos + 98, topPos + 66, 0xFFC6C6C6);
        minecraft.getTextureManager().bind(TEXTURE);
        for (int x : new int[]{25, 51, 77}) {
            blit(poseStack, leftPos + x, topPos + 46, 26, 46, 18, 18);
        }
        if (menu.getSlot(1).hasItem() && menu.getSlot(2).hasItem() && !menu.getSlot(3).hasItem()) {
            blit(poseStack, leftPos + 99, topPos + 45, 176, 0, 28, 21);
        }
    }
}
