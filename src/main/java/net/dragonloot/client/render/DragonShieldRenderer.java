package net.dragonloot.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dragonloot.DragonLoot;
import net.minecraft.client.renderer.entity.model.ShieldModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

public class DragonShieldRenderer extends ItemStackTileEntityRenderer {
    private static final RenderMaterial DRAGON_SHIELD_MATERIAL = new RenderMaterial(
            Atlases.SHIELD_SHEET,
            DragonLoot.id("entity/dragon_shield_base_nopattern"));

    private final ShieldModel shieldModel = new ShieldModel();

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType displayContext, MatrixStack poseStack,
                             IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        IVertexBuilder consumer = DRAGON_SHIELD_MATERIAL.sprite().wrap(ItemRenderer.getFoilBufferDirect(
                buffer,
                this.shieldModel.renderType(DRAGON_SHIELD_MATERIAL.atlasLocation()),
                true,
                stack.hasFoil()));
        this.shieldModel.handle().render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        this.shieldModel.plate().render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
