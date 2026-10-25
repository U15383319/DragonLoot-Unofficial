package net.dragonloot;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.dragonloot.compat.jei.DragonSmithingCategory;
import net.dragonloot.client.screen.DragonSmithingScreen;
import net.dragonloot.init.ModItems;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public class DragonLootClientSmokeTest {
    private static int stage;
    private static int ticks;

    @Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Setup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (Boolean.getBoolean("dragonloot.verifyClient")) {
                event.enqueueWork(() -> {
                    Minecraft client = Minecraft.getInstance();
                    client.options.setSoundCategoryVolume(SoundSource.MASTER, 0.0F);
                    client.getSoundManager().updateSourceVolume(SoundSource.MASTER, 0.0F);
                    client.options.pauseOnLostFocus = false;
                    GLFW.glfwIconifyWindow(client.getWindow().getWindow());
                });
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) throws Exception {
        if (!Boolean.getBoolean("dragonloot.verifyClient") || event.phase != TickEvent.Phase.END || stage == 8) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        ticks++;
        if (stage == 0 && client.screen instanceof TitleScreen && client.getOverlay() == null && ticks > 60) {
            client.options.renderDistance().set(2);
            client.options.guiScale().set(2);
            client.options.pauseOnLostFocus = false;
            var registry = RegistryAccess.builtinCopy();
            stage = 1;
            ticks = 0;
            client.createWorldOpenFlows().createFreshLevel("dragonloot-port-check-" + System.currentTimeMillis(),
                    new LevelSettings("DragonLoot port verification", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                            false, new GameRules(), DataPackConfig.DEFAULT),
                    registry, WorldPresets.createNormalWorldFromPreset(registry, 12345L, false, false));
        } else if (stage == 1 && client.level != null && client.player != null && ticks > 100) {
            verifyModels(client);
            client.getSingleplayerServer().execute(() -> {
                var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                player.getInventory().clearContent();
                player.getInventory().setItem(0, new ItemStack(ModItems.DRAGON_TRIDENT.get()));
                player.getInventory().setItem(1, new ItemStack(ModItems.DRAGON_SWORD.get()));
                player.getInventory().setItem(2, new ItemStack(ModItems.DRAGON_SHIELD.get()));
                int index = 3;
                for (var item : ForgeRegistries.ITEMS.getValues().stream()
                        .filter(item -> ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(DragonLoot.MOD_ID))
                        .filter(item -> item != ModItems.DRAGON_TRIDENT.get() && item != ModItems.DRAGON_SWORD.get()
                                && item != ModItems.DRAGON_SHIELD.get())
                        .sorted(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).toString())).toList()) {
                    player.getInventory().setItem(index++, new ItemStack(item));
                }
                player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.DRAGON_SHIELD.get()));
                player.inventoryMenu.broadcastChanges();
            });
            stage = 2;
            ticks = 0;
        } else if (stage == 2 && ticks > 60) {
            client.setScreen(new InventoryScreen(client.player));
            stage = 3;
            ticks = 0;
        } else if (stage == 3 && ticks > 40) {
            capture(client, "inventory");
            client.getSingleplayerServer().execute(() -> {
                var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                var pos = player.blockPosition().east();
                player.level.setBlock(pos, Blocks.SMITHING_TABLE.defaultBlockState(), 3);
                NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, user) ->
                        new DragonSmithingMenu(id, inventory, ContainerLevelAccess.create(player.level, pos)),
                        Component.translatable("container.upgrade")));
                player.containerMenu.getSlot(0).set(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()));
                player.containerMenu.getSlot(1).set(new ItemStack(Items.NETHERITE_SWORD));
                player.containerMenu.getSlot(2).set(new ItemStack(ModItems.DRAGON_SCALE.get()));
                player.containerMenu.broadcastChanges();
            });
            stage = 4;
            ticks = 0;
        } else if (stage == 4 && client.screen instanceof DragonSmithingScreen && ticks > 60) {
            capture(client, "smithing");
            if (JeiProbe.runtime == null || JeiProbe.runtime.getRecipeManager()
                    .createRecipeLookup(DragonSmithingCategory.TYPE).get().count() != 16) {
                throw new IllegalStateException("JEI must display all 16 template upgrade recipes");
            }
            JeiProbe.runtime.getRecipesGui().showTypes(List.of(DragonSmithingCategory.TYPE));
            stage = 9;
            ticks = 0;
        } else if (stage == 9 && ticks > 40) {
            capture(client, "jei-upgrades");
            client.player.closeContainer();
            client.setScreen(null);
            stage = 5;
            ticks = 0;
        } else if (stage == 5 && ticks > 50) {
            capture(client, "trident-held");
            client.options.keyUse.setDown(true);
            client.player.startUsingItem(InteractionHand.MAIN_HAND);
            client.getSingleplayerServer().execute(() ->
                    client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID())
                            .startUsingItem(InteractionHand.MAIN_HAND));
            stage = 6;
            ticks = 0;
        } else if (stage == 6 && ticks > 30) {
            if (!client.player.isUsingItem()) {
                throw new IllegalStateException("Trident use animation must remain active");
            }
            verifyModels(client);
            capture(client, "trident-throwing");
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            stage = 7;
            ticks = 0;
        } else if (stage == 7 && ticks > 30) {
            capture(client, "trident-third-person");
            LogUtils.getLogger().info("PORT_CLIENT_VERIFICATION_PASSED: models, shield atlas, inventory and smithing screen");
            stage = 8;
            client.options.keyUse.setDown(false);
            client.execute(client::stop);
        }
        if (ticks > 3600) {
            throw new IllegalStateException("Client port verification timed out at stage " + stage);
        }
    }

    private static void verifyModels(Minecraft client) {
        ItemStack trident = client.player.getMainHandItem().is(ModItems.DRAGON_TRIDENT.get())
                ? client.player.getMainHandItem() : new ItemStack(ModItems.DRAGON_TRIDENT.get());
        BakedModel model = client.getItemRenderer().getModel(trident, client.level, client.player, 0);
        for (var context : new ItemTransforms.TransformType[]{ItemTransforms.TransformType.GUI,
                ItemTransforms.TransformType.GROUND, ItemTransforms.TransformType.FIXED}) {
            if (model.applyTransform(context, new PoseStack(), false).isCustomRenderer()) {
                throw new IllegalStateException("Trident inventory model must stay 2D: " + context);
            }
        }
        for (var context : new ItemTransforms.TransformType[]{ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
                ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND}) {
            if (!model.applyTransform(context, new PoseStack(), false).isCustomRenderer()) {
                throw new IllegalStateException("Trident held model must use its 3D renderer: " + context);
            }
        }
        var texture = DragonLoot.id("entity/dragon_shield_base_nopattern");
        if (!client.getTextureAtlas(Sheets.SHIELD_SHEET).apply(texture).getName().equals(texture)) {
            throw new IllegalStateException("Dragon shield texture was not stitched");
        }
    }

    private static void capture(Minecraft client, String name) throws Exception {
        Path folder = client.gameDirectory.toPath().resolve("screenshots/port-verification");
        Files.createDirectories(folder);
        try (NativeImage screenshot = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
            screenshot.writeToFile(folder.resolve(name + ".png"));
        }
    }

    @JeiPlugin
    public static class JeiProbe implements IModPlugin {
        private static IJeiRuntime runtime;

        @Override
        public ResourceLocation getPluginUid() {
            return DragonLoot.id("client_verification");
        }

        @Override
        public void onRuntimeAvailable(IJeiRuntime runtime) {
            JeiProbe.runtime = runtime;
        }
    }
}
