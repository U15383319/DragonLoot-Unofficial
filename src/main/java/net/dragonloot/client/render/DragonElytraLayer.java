package net.dragonloot.client.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public final class DragonElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_elytra.png");
    private static final Set<LivingEntityRenderer<?, ?>> ATTACHED_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());
    private final ElytraModel<T> wings = new ElytraModel<>(createLayer().bakeRoot());

    private DragonElytraLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void attachLayer(RenderLivingEvent.Pre<T, M> event) {
        var renderer = event.getRenderer();
        if (event.getEntity().getItemBySlot(EquipmentSlot.CHEST).is(ModItems.UPGRADED_DRAGON_CHESTPLATE.get())
                && ATTACHED_RENDERERS.add(renderer)) {
            renderer.addLayer(new DragonElytraLayer<>(renderer));
        }
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
        poseStack.translate(0.0D, 0.0D, 0.02D);
        getParentModel().copyPropertiesTo(wings);
        wings.setupAnim(entity, limbSwing, limbAmount, age, headYaw, headPitch);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(
                buffers, RenderType.armorCutoutNoCull(TEXTURE), false, chestplate.hasFoil());
        wings.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
