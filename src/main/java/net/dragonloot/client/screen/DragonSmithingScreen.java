package net.dragonloot.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DragonSmithingScreen extends AbstractContainerScreen<DragonSmithingMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/smithing.png");

    public DragonSmithingScreen(DragonSmithingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = 44;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        fill(poseStack, leftPos + 24, topPos + 44, leftPos + 98, topPos + 66, 0xFFC6C6C6);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        for (int x : new int[]{25, 51, 77}) {
            blit(poseStack, leftPos + x, topPos + 46, 26, 46, 18, 18);
        }
        if (menu.getSlot(1).hasItem() && menu.getSlot(2).hasItem() && !menu.getSlot(3).hasItem()) {
            blit(poseStack, leftPos + 99, topPos + 45, 176, 0, 28, 21);
        }
    }
}
