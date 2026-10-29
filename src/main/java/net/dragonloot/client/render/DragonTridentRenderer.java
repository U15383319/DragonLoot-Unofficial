package net.dragonloot.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Vector3f;
import net.dragonloot.DragonLoot;
import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.client.renderer.entity.model.TridentModel;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class DragonTridentRenderer extends EntityRenderer<DragonTridentEntity> {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_trident.png");
    private final TridentModel model;

    public DragonTridentRenderer(EntityRendererManager manager) {
        super(manager);
        this.model = new TridentModel();
    }

    @Override
    public void render(DragonTridentEntity entity, float yaw, float partialTick, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTick, entity.yRotO, entity.yRot) - 90.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTick, entity.xRotO, entity.xRot) + 90.0F));
        IVertexBuilder consumer = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(getTextureLocation(entity)), false, entity.isFoil());
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonTridentEntity entity) {
        return TEXTURE;
    }
}
