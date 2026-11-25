package net.dragonloot.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class DragonHelmetModel extends HumanoidModel<LivingEntity> {
    private final ModelPart helmet;

    public DragonHelmetModel() {
        this(bakeModel());
    }

    private DragonHelmetModel(ModelPart root) {
        super(root);
        this.helmet = root.getChild("helmet");
    }

    private static ModelPart bakeModel() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        var helmet = mesh.getRoot().addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(0, 16).addBox(-3.0F, -7.0F, -7.0F, 6.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        helmet.addOrReplaceChild("left_lower_horn", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(-3.0F, -5.0F, 3.0F, 0.0F, -0.3927F, 0.0F));
        helmet.addOrReplaceChild("right_lower_horn", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-2.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(4.0F, -5.0F, 3.0F, 0.0F, 0.3927F, 0.0F));
        helmet.addOrReplaceChild("upper_horns", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F)
                .texOffs(24, 0).addBox(4.8F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(-2.9F, -8.0F, 2.0F, 0.7854F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                               int color) {
        if (!head.visible) {
            return;
        }

        poseStack.pushPose();
        head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -1.75F, 0.0F);
        poseStack.scale(1.19F, 1.19F, 1.19F);
        helmet.render(poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }
}
