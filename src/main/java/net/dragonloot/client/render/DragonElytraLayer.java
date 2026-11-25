package net.dragonloot.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dragonloot.DragonLoot;
import net.dragonloot.init.ModItems;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public final class DragonElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_elytra.png");
    private final ElytraModel<T> wings = new ElytraModel<>(createLayer().bakeRoot());

    private DragonElytraLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> addLayer(event.getSkin(skin)));
        event.getEntityTypes().forEach(type -> addLayer(event.getRenderer(type)));
    }

    private static void addLayer(EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            attachLayer(livingRenderer);
        }
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void attachLayer(LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new DragonElytraLayer<>(renderer));
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        CubeDeformation deformation = new CubeDeformation(1.0F);
        mesh.getRoot().addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(22, 0).addBox(-10.0F, 0.0F, 0.0F, 20.0F, 21.0F, 2.0F, deformation),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, -0.2617994F));
        mesh.getRoot().addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(22, 0).mirror().addBox(-10.0F, 0.0F, 0.0F, 20.0F, 21.0F, 2.0F, deformation),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, 0.2617994F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, T entity, float limbSwing,
                       float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestplate.is(ModItems.UPGRADED_DRAGON_CHESTPLATE.get())) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.02F);
        getParentModel().copyPropertiesTo(wings);
        wings.setupAnim(entity, limbSwing, limbAmount, age, headYaw, headPitch);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(
                buffers, RenderType.armorCutoutNoCull(TEXTURE), chestplate.hasFoil());
        wings.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
