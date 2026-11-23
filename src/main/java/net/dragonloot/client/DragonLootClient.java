package net.dragonloot.client;

import net.dragonloot.DragonLoot;
import net.dragonloot.client.render.DragonTridentBakedModel;
import net.dragonloot.client.render.DragonTridentRenderer;
import net.dragonloot.client.screen.DragonAnvilScreen;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.client.render.DragonShieldRenderer;
import net.dragonloot.client.render.DragonTridentItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public final class DragonLootClient {
    private static final ModelResourceLocation DRAGON_TRIDENT_MODEL =
            ModelResourceLocation.inventory(DragonLoot.id("dragon_trident"));
    private static final ModelResourceLocation DRAGON_TRIDENT_IN_HAND_MODEL =
            ModelResourceLocation.standalone(DragonLoot.id("item/dragon_trident_in_hand"));
    private static final ModelResourceLocation DRAGON_TRIDENT_THROWING_MODEL =
            ModelResourceLocation.standalone(DragonLoot.id("item/dragon_trident_throwing"));

    private DragonLootClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("cloth_config")) {
            ModList.get().getModContainerById(DragonLoot.MOD_ID).orElseThrow()
                    .registerExtensionPoint(IConfigScreenFactory.class,
                            (container, parent) -> DragonLootConfigScreen.create(parent));
        }

        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.DRAGON_BOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
                            : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemProperties.register(ModItems.DRAGON_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || CrossbowItem.isCharged(stack) ? 0.0F
                            : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack, entity));
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"),
                    (stack, level, entity, seed) -> entity != null && CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"),
                    (stack, level, entity, seed) -> entity != null && CrossbowItem.isCharged(stack) && stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_TRIDENT.get(), ResourceLocation.withDefaultNamespace("throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRAGON_SHIELD.get(), ResourceLocation.withDefaultNamespace("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DRAGON_ANVIL.get(), DragonAnvilScreen::new);
    }

    @SubscribeEvent
    public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private DragonShieldRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new DragonShieldRenderer();
                }
                return renderer;
            }
        }, ModItems.DRAGON_SHIELD);
        event.registerItem(new IClientItemExtensions() {
            private DragonTridentItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new DragonTridentItemRenderer();
                }
                return renderer;
            }
        }, ModItems.DRAGON_TRIDENT);
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
