package net.dragonloot.client.render;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dragonloot.DragonLoot;
import net.dragonloot.init.ModItems;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public final class DragonElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_elytra.png");
    private static final Set<LivingRenderer<?, ?>> ATTACHED_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());
    private final DragonElytraModel<T> wings = new DragonElytraModel<>();

    private DragonElytraLayer(LivingRenderer<T, M> renderer) {
        super(renderer);
    }

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void attachLayer(RenderLivingEvent.Pre<T, M> event) {
        var renderer = event.getRenderer();
        if (event.getEntity().getItemBySlot(EquipmentSlotType.CHEST).getItem() == ModItems.UPGRADED_DRAGON_CHESTPLATE.get()
                && ATTACHED_RENDERERS.add(renderer)) {
            renderer.addLayer(new DragonElytraLayer<>(renderer));
        }
    }

    @Override
    public void render(MatrixStack poseStack, IRenderTypeBuffer buffers, int light, T entity, float limbSwing,
                       float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        ItemStack chestplate = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestplate.getItem() != ModItems.UPGRADED_DRAGON_CHESTPLATE.get()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.02D);
        getParentModel().copyPropertiesTo(wings);
        wings.setupAnim(entity, limbSwing, limbAmount, age, headYaw, headPitch);
        IVertexBuilder consumer = ItemRenderer.getArmorFoilBuffer(
                buffers, RenderType.armorCutoutNoCull(TEXTURE), false, chestplate.hasFoil());
        wings.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static final class DragonElytraModel<T extends LivingEntity> extends AgeableModel<T> {
        private final ModelRenderer leftWing = new ModelRenderer(64, 32, 22, 0);
        private final ModelRenderer rightWing = new ModelRenderer(64, 32, 22, 0);
        private final List<ModelRenderer> wingParts = List.of(leftWing, rightWing);

        private DragonElytraModel() {
            leftWing.setPos(5.0F, 0.0F, 0.0F);
            leftWing.xRot = 0.2617994F;
            leftWing.zRot = -0.2617994F;
            leftWing.addBox(-10.0F, 0.0F, 0.0F, 20.0F, 21.0F, 2.0F, 1.0F);
            rightWing.setPos(-5.0F, 0.0F, 0.0F);
            rightWing.xRot = 0.2617994F;
            rightWing.zRot = 0.2617994F;
            rightWing.mirror = true;
            rightWing.addBox(-10.0F, 0.0F, 0.0F, 20.0F, 21.0F, 2.0F, 1.0F);
        }

        @Override
        protected Iterable<ModelRenderer> headParts() {
            return List.of();
        }

        @Override
        protected Iterable<ModelRenderer> bodyParts() {
            return wingParts;
        }

        @Override
        public void setupAnim(T entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
            float pitch = 0.2617994F;
            float roll = -0.2617994F;
            float yaw = 0.0F;
            float height = 0.0F;
            if (entity.isFallFlying()) {
                Vector3d velocity = entity.getDeltaMovement();
                float spread = velocity.y < 0.0D
                        ? 1.0F - (float) Math.pow(-velocity.normalize().y, 1.5D)
                        : 1.0F;
                pitch = spread * 0.34906584F + (1.0F - spread) * pitch;
                roll = spread * -1.5707964F + (1.0F - spread) * roll;
            } else if (entity.isCrouching()) {
                pitch = 0.6981317F;
                roll = -0.7853982F;
                yaw = 0.08726646F;
                height = 3.0F;
            }

            if (entity instanceof AbstractClientPlayerEntity) {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
                player.elytraRotX += (pitch - player.elytraRotX) * 0.1F;
                player.elytraRotY += (yaw - player.elytraRotY) * 0.1F;
                player.elytraRotZ += (roll - player.elytraRotZ) * 0.1F;
                pitch = player.elytraRotX;
                yaw = player.elytraRotY;
                roll = player.elytraRotZ;
            }

            leftWing.setPos(5.0F, height, 0.0F);
            leftWing.xRot = pitch;
            leftWing.yRot = yaw;
            leftWing.zRot = roll;
            rightWing.setPos(-5.0F, height, 0.0F);
            rightWing.xRot = pitch;
            rightWing.yRot = -yaw;
            rightWing.zRot = -roll;
        }
    }
}
