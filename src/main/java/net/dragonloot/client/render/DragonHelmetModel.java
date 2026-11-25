package net.dragonloot.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public final class DragonHelmetModel extends BipedModel<LivingEntity> {
    private final ModelRenderer helmet = new ModelRenderer(64, 32, 0, 0);

    public DragonHelmetModel() {
        super(0.0F);
        helmet.setPos(0.0F, 24.0F, 0.0F);
        helmet.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.1F);
        helmet.texOffs(0, 16).addBox(-3.0F, -7.0F, -7.0F, 6.0F, 4.0F, 3.0F);

        ModelRenderer leftLowerHorn = new ModelRenderer(64, 32, 34, 0);
        leftLowerHorn.setPos(-3.0F, -5.0F, 3.0F);
        leftLowerHorn.yRot = -0.3927F;
        leftLowerHorn.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F);
        helmet.addChild(leftLowerHorn);

        ModelRenderer rightLowerHorn = new ModelRenderer(64, 32, 34, 0);
        rightLowerHorn.setPos(4.0F, -5.0F, 3.0F);
        rightLowerHorn.yRot = 0.3927F;
        rightLowerHorn.addBox(-2.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F);
        helmet.addChild(rightLowerHorn);

        ModelRenderer upperHorns = new ModelRenderer(64, 32, 24, 0);
        upperHorns.setPos(-2.9F, -8.0F, 2.0F);
        upperHorns.xRot = 0.7854F;
        upperHorns.addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        upperHorns.addBox(4.8F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        helmet.addChild(upperHorns);
    }

    @Override
    public void renderToBuffer(MatrixStack poseStack, IVertexBuilder buffer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (!head.visible) {
            return;
        }

        poseStack.pushPose();
        head.translateAndRotate(poseStack);
        poseStack.translate(0.0D, -1.75D, 0.0D);
        poseStack.scale(1.19F, 1.19F, 1.19F);
        helmet.render(poseStack, buffer, light, overlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
