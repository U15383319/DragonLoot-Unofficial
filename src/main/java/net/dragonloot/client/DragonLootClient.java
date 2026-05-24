package net.dragonloot.client;

import net.dragonloot.DragonLoot;
import net.dragonloot.client.render.DragonTridentBakedModel;
import net.dragonloot.client.render.DragonTridentRenderer;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DragonLootClient {
    private static final ModelResourceLocation DRAGON_TRIDENT_MODEL =
            new ModelResourceLocation(DragonLoot.MOD_ID, "dragon_trident", "inventory");
    private static final ModelResourceLocation DRAGON_TRIDENT_IN_HAND_MODEL =
            new ModelResourceLocation(DragonLoot.MOD_ID, "dragon_trident_in_hand", "inventory");
    private static final ModelResourceLocation DRAGON_TRIDENT_THROWING_MODEL =
            new ModelResourceLocation(DragonLoot.MOD_ID, "dragon_trident_throwing", "inventory");

    private DragonLootClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.DRAGON_BOW.get(), new ResourceLocation("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
                            : (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemProperties.register(ModItems.DRAGON_BOW.get(), new ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("pull"),
                    (stack, level, entity, seed) -> entity == null || CrossbowItem.isCharged(stack) ? 0.0F
                            : (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack));
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("charged"),
                    (stack, level, entity, seed) -> entity != null && CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("firework"),
                    (stack, level, entity, seed) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_TRIDENT.get(), new ResourceLocation("throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_SHIELD.get(), new ResourceLocation("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_TRIDENT.get(), DragonTridentRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(DRAGON_TRIDENT_IN_HAND_MODEL);
        event.register(DRAGON_TRIDENT_THROWING_MODEL);
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        BakedModel baseModel = event.getModels().get(DRAGON_TRIDENT_MODEL);
        BakedModel inHandModel = event.getModels().get(DRAGON_TRIDENT_IN_HAND_MODEL);
        BakedModel throwingModel = event.getModels().get(DRAGON_TRIDENT_THROWING_MODEL);
        if (baseModel != null && inHandModel != null && throwingModel != null) {
            event.getModels().put(DRAGON_TRIDENT_MODEL, new DragonTridentBakedModel(baseModel, inHandModel, throwingModel));
        }
    }
}
