package net.dragonloot;

import net.minecraft.client.renderer.texture.NativeImage;
import com.mojang.blaze3d.matrix.MatrixStack;
import org.apache.logging.log4j.LogManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.dragonloot.compat.jei.DragonSmithingCategory;
import net.dragonloot.client.screen.DragonSmithingScreen;
import net.dragonloot.init.ModItems;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Hand;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.NetworkHooks;
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
    private static int projectileId;
    private static int targetId;

    @Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Setup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (Boolean.getBoolean("dragonloot.verifyClient")) {
                event.enqueueWork(() -> {
                    Minecraft client = Minecraft.getInstance();
                    client.options.setSoundCategoryVolume(SoundCategory.MASTER, 0.0F);
                    client.getSoundManager().updateSourceVolume(SoundCategory.MASTER, 0.0F);
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
        if (stage == 0 && client.screen instanceof MainMenuScreen && client.getOverlay() == null && ticks > 60) {
            client.options.renderDistance = 2;
            client.options.guiScale = 2;
            client.options.pauseOnLostFocus = false;
            var registry = DynamicRegistries.builtin();
            stage = 1;
            ticks = 0;
            client.createLevel("dragonloot-port-check-" + System.currentTimeMillis(),
                    new WorldSettings("DragonLoot port verification", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                            false, new GameRules(), DatapackCodec.DEFAULT),
                    registry, DimensionGeneratorSettings.create(registry, new java.util.Properties())
                            .withSeed(false, java.util.OptionalLong.of(12345L)));
        } else if (stage == 1 && client.level != null && client.player != null && ticks > 100) {
            verifyModels(client);
            client.getSingleplayerServer().submit(() ->
                    DragonLootGameTests.runAll(client.getSingleplayerServer().overworld())).join();
            client.getSingleplayerServer().execute(() -> {
                var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                player.inventory.clearContent();
                player.inventory.setItem(0, new ItemStack(ModItems.DRAGON_TRIDENT.get()));
                player.inventory.setItem(1, new ItemStack(ModItems.DRAGON_SWORD.get()));
                player.inventory.setItem(2, new ItemStack(ModItems.DRAGON_SHIELD.get()));
                int index = 3;
                for (var item : ForgeRegistries.ITEMS.getValues().stream()
                        .filter(item -> ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(DragonLoot.MOD_ID))
                        .filter(item -> item != ModItems.DRAGON_TRIDENT.get() && item != ModItems.DRAGON_SWORD.get()
                                && item != ModItems.DRAGON_SHIELD.get())
                        .sorted(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).toString()))
                        .collect(java.util.stream.Collectors.toList())) {
                    player.inventory.setItem(index++, new ItemStack(item));
                }
                player.setItemSlot(EquipmentSlotType.OFFHAND, new ItemStack(ModItems.DRAGON_SHIELD.get()));
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
                NetworkHooks.openGui(player, new SimpleNamedContainerProvider((id, inventory, user) ->
                        new DragonSmithingMenu(id, inventory, IWorldPosCallable.create(player.level, pos)),
                        new TranslationTextComponent("container.upgrade")));
                player.containerMenu.getSlot(0).set(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()));
                player.containerMenu.getSlot(1).set(new ItemStack(Items.NETHERITE_SWORD));
                player.containerMenu.getSlot(2).set(new ItemStack(ModItems.DRAGON_SCALE.get()));
                player.containerMenu.broadcastChanges();
            });
            stage = 4;
            ticks = 0;
        } else if (stage == 4 && client.screen instanceof DragonSmithingScreen && ticks > 60) {
            capture(client, "smithing");
            if (JeiProbe.runtime == null) {
                throw new IllegalStateException("JEI runtime must be available");
            }
            var categories = JeiProbe.runtime.getRecipeManager().getRecipeCategories(List.of(DragonSmithingCategory.TYPE));
            if (categories.size() != 1 || JeiProbe.runtime.getRecipeManager().getRecipes(categories.get(0)).size() != 16) {
                throw new IllegalStateException("JEI must display all 16 template upgrade recipes");
            }
            JeiProbe.runtime.getRecipesGui().showCategories(List.of(DragonSmithingCategory.TYPE));
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
            client.player.startUsingItem(Hand.MAIN_HAND);
            client.getSingleplayerServer().execute(() ->
                    client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID())
                            .startUsingItem(Hand.MAIN_HAND));
            stage = 6;
            ticks = 0;
        } else if (stage == 6 && ticks > 30) {
            if (!client.player.isUsingItem()) {
                throw new IllegalStateException("Trident use animation must remain active");
            }
            verifyModels(client);
            capture(client, "trident-throwing");
            client.options.setCameraType(PointOfView.THIRD_PERSON_BACK);
            stage = 7;
            ticks = 0;
        } else if (stage == 7 && ticks > 30) {
            capture(client, "trident-third-person");
            client.options.keyUse.setDown(false);
            client.player.stopUsingItem();
            client.options.setCameraType(PointOfView.FIRST_PERSON);
            projectileId = client.getSingleplayerServer().submit(() -> {
                var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                player.stopUsingItem();
                var trident = new net.dragonloot.entity.DragonTridentEntity(player.level, player,
                        new ItemStack(ModItems.DRAGON_TRIDENT.get()));
                var pos = player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(3.0D));
                trident.setPos(pos.x, pos.y, pos.z);
                trident.setNoGravity(true);
                player.level.addFreshEntity(trident);
                return trident.getId();
            }).join();
            stage = 10;
            ticks = 0;
        } else if (stage == 10 && ticks > 30) {
            if (!(client.level.getEntity(projectileId) instanceof net.dragonloot.entity.DragonTridentEntity)) {
                throw new IllegalStateException("Thrown dragon trident must spawn on the client");
            }
            capture(client, "trident-entity");
            targetId = client.getSingleplayerServer().submit(() -> {
                var server = client.getSingleplayerServer();
                var player = server.getPlayerList().getPlayer(client.player.getUUID());
                var world = server.overworld();
                world.getEntity(projectileId).remove();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 8; z++) {
                        world.setBlock(new net.minecraft.util.math.BlockPos(x, 119, z), Blocks.STONE.defaultBlockState(), 3);
                        for (int y = 120; y <= 124; y++) {
                            world.setBlock(new net.minecraft.util.math.BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
                player.setGameMode(GameType.SURVIVAL);
                player.teleportTo(world, 0.5D, 120.0D, 0.5D, 0.0F, 0.0F);
                player.inventory.selected = 1;
                player.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
                var target = net.minecraft.entity.EntityType.COW.create(world);
                target.setPos(0.5D, 120.5D, 4.1D);
                target.setNoAi(true);
                target.setNoGravity(true);
                world.addFreshEntity(target);
                return target.getId();
            }).join();
            client.player.inventory.selected = 1;
            stage = 11;
            ticks = 0;
        } else if (stage == 11 && ticks > 60) {
            var hit = net.dragonloot.item.DragonSwordReach.pick(client.player, 1.0F);
            if (hit == null || hit.getEntity().getId() != targetId
                    || client.player.getEyePosition(1.0F).distanceToSqr(hit.getLocation()) <= 9.0D) {
                throw new IllegalStateException("Sword must select the extended target beyond vanilla range");
            }
            var click = new net.minecraftforge.client.event.InputEvent.ClickInputEvent(0, client.options.keyAttack, Hand.MAIN_HAND);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(click);
            if (!click.isCanceled()) {
                throw new IllegalStateException("Extended attack must use the validated server packet");
            }
            stage = 12;
            ticks = 0;
        } else if (stage == 12 && ticks > 20) {
            boolean damaged = client.getSingleplayerServer().submit(() -> {
                var target = client.getSingleplayerServer().overworld().getEntity(targetId);
                return target == null || ((net.minecraft.entity.LivingEntity) target).getHealth() < 10.0F;
            }).join();
            if (!damaged) {
                throw new IllegalStateException("Extended sword attack must damage the target on the server");
            }
            capture(client, "sword-reach");
            LogManager.getLogger().info("PORT_CLIENT_VERIFICATION_PASSED: models, shield atlas, inventory, smithing, projectile sync and sword reach attack");
            stage = 8;
            client.execute(client::stop);
        }
        if (ticks > 3600) {
            throw new IllegalStateException("Client port verification timed out at stage " + stage);
        }
    }

    private static void verifyModels(Minecraft client) {
        ItemStack trident = client.player.getMainHandItem().getItem() == ModItems.DRAGON_TRIDENT.get()
                ? client.player.getMainHandItem() : new ItemStack(ModItems.DRAGON_TRIDENT.get());
        IBakedModel model = client.getItemRenderer().getModel(trident, client.level, client.player);
        for (var context : new ItemCameraTransforms.TransformType[]{ItemCameraTransforms.TransformType.GUI,
                ItemCameraTransforms.TransformType.GROUND, ItemCameraTransforms.TransformType.FIXED}) {
            if (model.handlePerspective(context, new MatrixStack()).isCustomRenderer()) {
                throw new IllegalStateException("Trident inventory model must stay 2D: " + context);
            }
        }
        for (var context : new ItemCameraTransforms.TransformType[]{ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND}) {
            if (!model.handlePerspective(context, new MatrixStack()).isCustomRenderer()) {
                throw new IllegalStateException("Trident held model must use its 3D renderer: " + context);
            }
        }
        var texture = DragonLoot.id("entity/dragon_shield_base_nopattern");
        if (!client.getTextureAtlas(Atlases.SHIELD_SHEET).apply(texture).getName().equals(texture)) {
            throw new IllegalStateException("Dragon shield texture was not stitched");
        }
    }

    private static void capture(Minecraft client, String name) throws Exception {
        Path folder = client.gameDirectory.toPath().resolve("screenshots/port-verification");
        Files.createDirectories(folder);
        try (NativeImage screenshot = ScreenShotHelper.takeScreenshot(client.getWindow().getWidth(),
                client.getWindow().getHeight(), client.getMainRenderTarget())) {
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
