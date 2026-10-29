package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.entity.DragonTridentEntity;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.util.math.BlockPos;
import net.dragonloot.init.ModAttributes;
import net.dragonloot.item.DragonSwordReach;
import net.minecraft.world.server.ServerWorld;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import io.netty.buffer.Unpooled;

public class DragonLootGameTests {
    public static void runAll(ServerWorld world) {
        TestTrackerHolder helper = new TestTrackerHolder(world);
        upgradeRecipesAndNetwork(helper);
        smithingConsumesInputsAndPreservesEnchantments(helper);
        vanillaSmithingStillWorks(helper);
        swordReachFollowsMainHand(helper);
        shieldCooldownUsesConfiguredMultiplier(helper);
        tridentThrownDamageIncludesDragonBonus(helper);
        templateDuplicationAndEndCityLoot(helper);
        org.apache.logging.log4j.LogManager.getLogger().info("PORT_BEHAVIOR_VERIFICATION_PASSED: {} tests", helper.passed);
    }

    public static void upgradeRecipesAndNetwork(TestTrackerHolder helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.DRAGON_SMITHING);
        check(helper, recipes.size() == 16, "All 16 dragon upgrade recipes must load");
        for (DragonSmithingRecipe recipe : recipes) {
            ItemStack base = recipe.getBase().getItems()[0].copy();
            base.setHoverName(new StringTextComponent("Port verification"));
            var input = new Inventory(recipe.getTemplate().getItems()[0].copy(), base,
                    recipe.getAddition().getItems()[0].copy());
            check(helper, recipe.matches(input, helper.getLevel()), "Upgrade recipe must match: " + recipe.getId());
            ItemStack output = recipe.assemble(input);
            check(helper, output.getItem() == recipe.getResultItem().getItem() && output.hasCustomHoverName(),
                    "Upgrade must retain base NBT: " + recipe.getId());
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                var serializer = new DragonSmithingRecipe.Serializer();
                serializer.toNetwork(buffer, recipe);
                var received = serializer.fromNetwork(recipe.getId(), buffer);
                check(helper, received.matches(input, helper.getLevel())
                        && ItemStack.matches(received.assemble(input), output), "Recipe must sync to clients");
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    public static void smithingConsumesInputsAndPreservesEnchantments(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        DragonSmithingMenu menu = new DragonSmithingMenu(0, player.inventory);
        ItemStack base = new ItemStack(Items.NETHERITE_SWORD);
        base.enchant(Enchantments.SHARPNESS, 4);
        menu.getSlot(0).set(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get(), 2));
        menu.getSlot(1).set(base);
        menu.getSlot(2).set(new ItemStack(ModItems.DRAGON_SCALE.get(), 2));
        check(helper, menu.getSlot(3).getItem().getItem() == ModItems.DRAGON_SWORD.get(), "Template smithing must produce dragon sword");
        ItemStack taken = menu.quickMoveStack(player, 3);
        check(helper, taken.getItem() == ModItems.DRAGON_SWORD.get() && taken.isEnchanted(), "Shift-click must preserve enchantments");
        check(helper, menu.getSlot(0).getItem().getCount() == 1 && menu.getSlot(1).getItem().isEmpty()
                && menu.getSlot(2).getItem().getCount() == 1, "One template, base and scale must be consumed");
        check(helper, menu.getSlot(3).getItem().isEmpty(), "Taking output must not duplicate it");
        menu.getSlot(1).set(new ItemStack(Items.NETHERITE_SWORD));
        menu.getSlot(0).set(ItemStack.EMPTY);
        check(helper, menu.getSlot(3).getItem().isEmpty(), "Dragon upgrade must require a template");
        helper.succeed();
    }

    public static void vanillaSmithingStillWorks(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        DragonSmithingMenu menu = new DragonSmithingMenu(0, player.inventory);
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(2).set(new ItemStack(Items.NETHERITE_INGOT));
        check(helper, menu.getSlot(3).getItem().getItem() == Items.NETHERITE_SWORD, "Original two-input netherite recipe must work");
        check(helper, menu.quickMoveStack(player, 3).getItem() == Items.NETHERITE_SWORD, "Vanilla output must be collectible");
        check(helper, menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty(), "Vanilla inputs must be consumed");
        helper.succeed();
    }

    public static void swordReachFollowsMainHand(TestTrackerHolder helper) {
        ForgeConfigSpec.DoubleValue config = DragonLootConfig.SPEC.getValues().get("dragon_sword_entity_reach_bonus");
        double original = config.get();
        config.set(1.25D);
        try {
            verifySwordReach(helper);
        } finally {
            config.set(original);
        }
        helper.succeed();
    }

    private static void verifySwordReach(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        double baseline = player.getAttributeValue(ModAttributes.ATTACK_RANGE_BONUS.get());
        double blockReach = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        player.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
        player.tick();
        check(helper, Math.abs(player.getAttributeValue(ModAttributes.ATTACK_RANGE_BONUS.get())
                - baseline - DragonLootConfig.dragonSwordEntityReachBonus()) < 0.0001D, "Holding dragon sword must add configured reach");
        player.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        player.tick();
        check(helper, player.getAttributeValue(ModAttributes.ATTACK_RANGE_BONUS.get()) == baseline, "Unequipping sword must restore reach");
        player.setItemSlot(EquipmentSlotType.OFFHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
        player.tick();
        check(helper, player.getAttributeValue(ModAttributes.ATTACK_RANGE_BONUS.get()) == baseline, "Offhand sword must not add reach");
        check(helper, player.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) == blockReach, "Sword must not increase block reach");
        verifyReachTargeting(helper, player);
    }

    public static void shieldCooldownUsesConfiguredMultiplier(TestTrackerHolder helper) {
        ForgeConfigSpec.DoubleValue config = DragonLootConfig.SPEC.getValues().get("dragon_shield_disable_cooldown_multiplier");
        double original = config.get();
        config.set(0.25D);
        try {
            verifyShieldCooldown(helper);
        } finally {
            config.set(original);
        }
        helper.succeed();
    }

    private static void verifyShieldCooldown(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        player.getCooldowns().addCooldown(ModItems.DRAGON_SHIELD.get(), 200);
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        int expectedTicks = Math.max(1, (int) Math.ceil(200 * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
        for (int i = 0; i < expectedTicks - 1; i++) {
            player.getCooldowns().tick();
        }
        check(helper, player.getCooldowns().isOnCooldown(ModItems.DRAGON_SHIELD.get()), "Cooldown must last the configured duration");
        player.getCooldowns().tick();
        check(helper, !player.getCooldowns().isOnCooldown(ModItems.DRAGON_SHIELD.get()), "Reduced cooldown must expire on time");
    }

    public static void tridentThrownDamageIncludesDragonBonus(TestTrackerHolder helper) {
        LivingEntity target = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        float before = target.getHealth();
        TestTrident trident = new TestTrident(helper.getLevel(), helper.makeMockPlayer());
        trident.hit(target);
        double expected = 8.0D + DragonLootConfig.itemBaseDamage() / 5.0D;
        check(helper, Math.abs(before - target.getHealth() - expected) < 0.0001D, "Thrown trident must apply dragon damage bonus");
        helper.succeed();
    }

    public static void templateDuplicationAndEndCityLoot(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        CraftingInventory grid = new CraftingInventory(new DragonSmithingMenu(0, player.inventory), 3, 3);
        for (int i = 0; i < 9; i++) {
            grid.setItem(i, new ItemStack(i == 1 ? ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()
                    : i == 4 ? Items.PURPUR_BLOCK : Items.CHORUS_FRUIT));
        }
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, grid, helper.getLevel());
        check(helper, recipe.isPresent() && recipe.get().assemble(grid).getItem() == ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()
                && recipe.get().assemble(grid).getCount() == 2, "Template duplication recipe must return two templates");
        var loot = helper.getLevel().getServer().getLootTables().get(new ResourceLocation("minecraft", "chests/end_city_treasure"));
        check(helper, loot.getPool("dragonloot_dragon_upgrade_template") != null, "End City chest loot must include the template pool");
        helper.succeed();
    }

    private static void check(TestTrackerHolder helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static void verifyReachTargeting(TestTrackerHolder helper, PlayerEntity player) {
        helper.world.getEntitiesOfClass(net.minecraft.entity.MobEntity.class,
                new net.minecraft.util.math.AxisAlignedBB(-8, 90, -8, 8, 110, 16))
                .forEach(net.minecraft.entity.Entity::remove);
        player.setPos(0.5D, 100.0D, 0.5D);
        player.xRot = 0.0F;
        player.yRot = 0.0F;
        player.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
        player.tick();
        LivingEntity target = helper.spawn(EntityType.COW, new BlockPos(0, 100, 4));
        target.setPos(0.5D, 100.5D, 4.4D);
        check(helper, DragonSwordReach.pick(player, 1.0F) != null, "Extended reach must select targets beyond three blocks");
        helper.world.setBlock(new BlockPos(0, 101, 2), net.minecraft.block.Blocks.STONE.defaultBlockState(), 3);
        check(helper, DragonSwordReach.pick(player, 1.0F) == null, "Extended reach must not pass through walls");
        helper.world.setBlock(new BlockPos(0, 101, 2), net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);
        target.setPos(0.5D, 100.5D, 8.0D);
        check(helper, DragonSwordReach.pick(player, 1.0F) == null, "Extended reach must reject distant targets");
        player.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        player.tick();
        target.setPos(0.5D, 100.5D, 4.4D);
        check(helper, DragonSwordReach.pick(player, 1.0F) == null, "Unequipping must disable extended targeting immediately");
        target.remove();
    }

    private static final class TestTrackerHolder {
        private final ServerWorld world;
        private int passed;

        private TestTrackerHolder(ServerWorld world) {
            this.world = world;
        }

        private ServerWorld getLevel() {
            return world;
        }

        private PlayerEntity makeMockPlayer() {
            return new PlayerEntity(world, new BlockPos(0, 100, 0), 0.0F,
                    new GameProfile(UUID.randomUUID(), "PortTest")) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
        }

        private <T extends LivingEntity> T spawn(EntityType<T> type, BlockPos pos) {
            T entity = type.create(world);
            entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            if (entity instanceof net.minecraft.entity.MobEntity) {
                ((net.minecraft.entity.MobEntity) entity).setNoAi(true);
            }
            world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            world.addFreshEntity(entity);
            return entity;
        }

        private void succeed() {
            passed++;
        }

        private void fail(String message) {
            throw new AssertionError(message);
        }
    }

    private static class TestTrident extends DragonTridentEntity {
        private TestTrident(World level, LivingEntity owner) {
            super(level, owner, new ItemStack(ModItems.DRAGON_TRIDENT.get()));
        }

        private void hit(LivingEntity target) {
            onHitEntity(new EntityRayTraceResult(target));
        }
    }
}
