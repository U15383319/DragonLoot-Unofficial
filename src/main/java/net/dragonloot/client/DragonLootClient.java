package net.dragonloot.client;

import net.dragonloot.DragonLoot;
import net.dragonloot.client.render.DragonTridentBakedModel;
import net.dragonloot.client.render.DragonTridentRenderer;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.client.screen.DragonSmithingScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.event.TextureStitchEvent;
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
            MenuScreens.register(ModMenus.DRAGON_SMITHING.get(), DragonSmithingScreen::new);
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
    public static void registerAdditionalModels(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(DRAGON_TRIDENT_IN_HAND_MODEL);
        ForgeModelBakery.addSpecialModel(DRAGON_TRIDENT_THROWING_MODEL);
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelBakeEvent event) {
        BakedModel baseModel = event.getModelRegistry().get(DRAGON_TRIDENT_MODEL);
        BakedModel inHandModel = event.getModelRegistry().get(DRAGON_TRIDENT_IN_HAND_MODEL);
        BakedModel throwingModel = event.getModelRegistry().get(DRAGON_TRIDENT_THROWING_MODEL);
        if (baseModel != null && inHandModel != null && throwingModel != null) {
            event.getModelRegistry().put(DRAGON_TRIDENT_MODEL, new DragonTridentBakedModel(baseModel, inHandModel, throwingModel));
        }
    }

    @SubscribeEvent
    public static void registerShieldTexture(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(Sheets.SHIELD_SHEET)) {
            event.addSprite(DragonLoot.id("entity/dragon_shield_base_nopattern"));
        }
    }
}
