package net.dragonloot.client;

import net.dragonloot.DragonLoot;
import net.dragonloot.client.render.DragonTridentBakedModel;
import net.dragonloot.client.render.DragonTridentRenderer;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.client.screen.DragonSmithingScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DragonLootClient {
    private static final ModelResourceLocation DRAGON_TRIDENT_MODEL =
            new ModelResourceLocation(DragonLoot.id("dragon_trident"), "inventory");
    private static final ModelResourceLocation DRAGON_TRIDENT_IN_HAND_MODEL =
            new ModelResourceLocation(DragonLoot.id("dragon_trident_in_hand"), "inventory");
    private static final ModelResourceLocation DRAGON_TRIDENT_THROWING_MODEL =
            new ModelResourceLocation(DragonLoot.id("dragon_trident_throwing"), "inventory");

    private DragonLootClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DRAGON_TRIDENT.get(), DragonTridentRenderer::new);
        event.enqueueWork(() -> {
            ScreenManager.register(ModMenus.DRAGON_SMITHING.get(), DragonSmithingScreen::new);
            ItemModelsProperties.register(ModItems.DRAGON_BOW.get(), new ResourceLocation("pull"),
                    (stack, level, entity) -> entity == null || entity.getUseItem() != stack ? 0.0F
                            : (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemModelsProperties.register(ModItems.DRAGON_BOW.get(), new ResourceLocation("pulling"),
                    (stack, level, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemModelsProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("pull"),
                    (stack, level, entity) -> entity == null || CrossbowItem.isCharged(stack) ? 0.0F
                            : (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack));
            ItemModelsProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("pulling"),
                    (stack, level, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemModelsProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("charged"),
                    (stack, level, entity) -> entity != null && CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemModelsProperties.register(ModItems.DRAGON_CROSSBOW.get(), new ResourceLocation("firework"),
                    (stack, level, entity) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
            ItemModelsProperties.register(ModItems.DRAGON_TRIDENT.get(), new ResourceLocation("throwing"),
                    (stack, level, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemModelsProperties.register(ModItems.DRAGON_SHIELD.get(), new ResourceLocation("blocking"),
                    (stack, level, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelRegistryEvent event) {
        ModelLoader.addSpecialModel(DRAGON_TRIDENT_IN_HAND_MODEL);
        ModelLoader.addSpecialModel(DRAGON_TRIDENT_THROWING_MODEL);
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelBakeEvent event) {
        IBakedModel baseModel = event.getModelRegistry().get(DRAGON_TRIDENT_MODEL);
        IBakedModel inHandModel = event.getModelRegistry().get(DRAGON_TRIDENT_IN_HAND_MODEL);
        IBakedModel throwingModel = event.getModelRegistry().get(DRAGON_TRIDENT_THROWING_MODEL);
        if (baseModel != null && inHandModel != null && throwingModel != null) {
            event.getModelRegistry().put(DRAGON_TRIDENT_MODEL, new DragonTridentBakedModel(baseModel, inHandModel, throwingModel));
        }
    }

    @SubscribeEvent
    public static void registerShieldTexture(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(Atlases.SHIELD_SHEET)) {
            event.addSprite(DragonLoot.id("entity/dragon_shield_base_nopattern"));
        }
    }
}
