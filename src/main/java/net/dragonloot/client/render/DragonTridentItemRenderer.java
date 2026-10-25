package net.dragonloot.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dragonloot.DragonLoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class DragonTridentItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_trident.png");

    private TridentModel model;

    public DragonTridentItemRenderer() {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private DragonTridentItemRenderer(BlockEntityRenderDispatcher dispatcher, net.minecraft.client.model.geom.EntityModelSet models) {
        super(dispatcher, models);
        this.model = new TridentModel(models.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.model = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(
                buffer,
                this.model.renderType(TEXTURE),
                false,
                stack.hasFoil());
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
