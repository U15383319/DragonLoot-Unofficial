package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.entity.DragonTridentEntity;
import net.dragonloot.init.ModItems;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
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
        advancementsRequireTheirItems(helper);
        upgradeRecipesAndNetwork(helper);
        smithingConsumesInputsAndPreservesEnchantments(helper);
        vanillaSmithingStillWorks(helper);
        swordReachFollowsMainHand(helper);
        shieldCooldownUsesConfiguredMultiplier(helper);
        tridentThrownDamageIncludesDragonBonus(helper);
        endCityLootHasNoDragonUpgradePool(helper);
        org.apache.logging.log4j.LogManager.getLogger().info("PORT_BEHAVIOR_VERIFICATION_PASSED: {} tests", helper.passed);
    }

    private static void advancementsRequireTheirItems(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        var advancements = helper.getLevel().getServer().getAdvancements();
        var armor = (InventoryChangeTrigger.Instance) advancements.getAdvancement(DragonLoot.id("dragon_armor"))
                .getCriteria().get("dragon_armor").getTrigger();
        var recipes = (InventoryChangeTrigger.Instance) advancements.getAdvancement(DragonLoot.id("dragon_recipes"))
                .getCriteria().get("dragon_recipes").getTrigger();
        int slots = player.inventory.getContainerSize();
        ItemStack dirt = new ItemStack(Items.DIRT);
        player.inventory.setItem(0, dirt);
        check(helper, !armor.matches(player.inventory, dirt, 0, slots - 1, 1), "Dirt must not grant the dragon armor advancement");
        check(helper, !recipes.matches(player.inventory, dirt, 0, slots - 1, 1), "Dirt must not unlock dragon recipes");

        player.inventory.setItem(0, new ItemStack(ModItems.DRAGON_HELMET.get()));
        player.inventory.setItem(1, new ItemStack(ModItems.DRAGON_CHESTPLATE.get()));
        ItemStack leggings = new ItemStack(ModItems.DRAGON_LEGGINGS.get());
        player.inventory.setItem(2, leggings);
        check(helper, !armor.matches(player.inventory, leggings, 0, slots - 3, 3), "Three armor pieces must not complete the advancement");
        ItemStack boots = new ItemStack(ModItems.DRAGON_BOOTS.get());
        player.inventory.setItem(3, boots);
        check(helper, armor.matches(player.inventory, boots, 0, slots - 4, 4), "All four dragon armor pieces must complete the advancement");

        ItemStack scale = new ItemStack(ModItems.DRAGON_SCALE.get());
        player.inventory.setItem(4, scale);
        check(helper, recipes.matches(player.inventory, scale, 0, slots - 5, 5), "A dragon scale must unlock dragon recipes");
        helper.succeed();
    }

    public static void upgradeRecipesAndNetwork(TestTrackerHolder helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(IRecipeType.SMITHING).stream()
                .filter(recipe -> recipe.getId().getNamespace().equals(DragonLoot.MOD_ID))
                .collect(java.util.stream.Collectors.toList());
        check(helper, recipes.size() == 16, "All 16 dragon upgrades must load as vanilla smithing recipes");
        PlayerEntity player = helper.makeMockPlayer();
        SmithingTableContainer menu = new SmithingTableContainer(0, player.inventory);
        var serializer = IRecipeSerializer.SMITHING;
        for (var recipe : recipes) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                serializer.toNetwork(buffer, recipe);
                ItemStack base = Ingredient.fromNetwork(buffer).getItems()[0].copy();
                ItemStack addition = Ingredient.fromNetwork(buffer).getItems()[0].copy();
                base.setHoverName(new StringTextComponent("Port verification"));
                var input = new Inventory(base, addition);
                check(helper, recipe.matches(input, helper.getLevel()), "Upgrade recipe must match: " + recipe.getId());
                menu.getSlot(0).set(base);
                menu.getSlot(1).set(addition);
                ItemStack output = menu.getSlot(2).getItem();
                check(helper, output.getItem() == recipe.getResultItem().getItem() && output.hasCustomHoverName(),
                        "The vanilla smithing menu must show the expected result and preserve item data: " + recipe.getId());
                buffer.readerIndex(0);
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
        SmithingTableContainer menu = new SmithingTableContainer(0, player.inventory);
        ItemStack base = new ItemStack(Items.NETHERITE_SWORD);
        base.enchant(Enchantments.SHARPNESS, 4);
        menu.getSlot(0).set(base);
        check(helper, menu.getSlot(2).getItem().isEmpty(), "Dragon smithing must require an upgrade material");
        menu.getSlot(1).set(new ItemStack(ModItems.DRAGON_SCALE.get(), 2));
        check(helper, menu.getSlot(2).getItem().getItem() == ModItems.DRAGON_SWORD.get(), "Smithing must produce the dragon sword");
        ItemStack taken = menu.quickMoveStack(player, 2);
        check(helper, taken.getItem() == ModItems.DRAGON_SWORD.get() && taken.isEnchanted(), "Shift-click must preserve enchantments");
        check(helper, menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().getCount() == 1,
                "Smithing must consume one base item and one dragon scale");
        check(helper, menu.getSlot(2).getItem().isEmpty(), "Taking output must not duplicate it");
        helper.succeed();
    }

    public static void vanillaSmithingStillWorks(TestTrackerHolder helper) {
        PlayerEntity player = helper.makeMockPlayer();
        SmithingTableContainer menu = new SmithingTableContainer(0, player.inventory);
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(1).set(new ItemStack(Items.NETHERITE_INGOT));
        check(helper, menu.getSlot(2).getItem().getItem() == Items.NETHERITE_SWORD, "Original two-input netherite recipe must work");
        check(helper, menu.quickMoveStack(player, 2).getItem() == Items.NETHERITE_SWORD, "Vanilla output must be collectible");
        check(helper, menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "Vanilla inputs must be consumed");
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

    public static void endCityLootHasNoDragonUpgradePool(TestTrackerHolder helper) {
        var loot = helper.getLevel().getServer().getLootTables().get(new ResourceLocation("minecraft", "chests/end_city_treasure"));
        check(helper, loot.getPool("dragonloot_dragon_upgrade_template") == null, "End City loot must use its vanilla pools");
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
