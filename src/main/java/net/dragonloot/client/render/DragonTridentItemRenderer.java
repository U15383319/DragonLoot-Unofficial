package net.dragonloot.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dragonloot.DragonLoot;
import net.minecraft.client.renderer.entity.model.TridentModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

public class DragonTridentItemRenderer extends ItemStackTileEntityRenderer {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_trident.png");

    private final TridentModel model = new TridentModel();

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType displayContext, MatrixStack poseStack,
                             IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        IVertexBuilder consumer = ItemRenderer.getFoilBufferDirect(
                buffer,
                this.model.renderType(TEXTURE),
                false,
                stack.hasFoil());
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
