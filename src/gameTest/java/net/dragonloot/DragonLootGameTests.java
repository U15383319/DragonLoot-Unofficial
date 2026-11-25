package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.entity.DragonTridentEntity;
import net.dragonloot.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import io.netty.buffer.Unpooled;

@GameTestHolder(DragonLoot.MOD_ID)
@PrefixGameTestTemplate(false)
public class DragonLootGameTests {
    @GameTest(template = "empty")
    public static void upgradeRecipesAndNetwork(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.SMITHING).stream()
                .filter(recipe -> recipe.getId().getNamespace().equals(DragonLoot.MOD_ID))
                .collect(java.util.stream.Collectors.toList());
        check(helper, recipes.size() == 16, "All 16 dragon upgrades must load as vanilla smithing recipes");
        Player player = helper.makeMockPlayer();
        SmithingMenu menu = new SmithingMenu(0, player.getInventory());
        var serializer = RecipeSerializer.SMITHING;
        for (var recipe : recipes) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                serializer.toNetwork(buffer, recipe);
                ItemStack base = Ingredient.fromNetwork(buffer).getItems()[0].copy();
                ItemStack addition = Ingredient.fromNetwork(buffer).getItems()[0].copy();
                base.setHoverName(Component.literal("Port verification"));
                var input = new SimpleContainer(base, addition);
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

    @GameTest(template = "empty")
    public static void smithingConsumesInputsAndPreservesEnchantments(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        SmithingMenu menu = new SmithingMenu(0, player.getInventory());
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

    @GameTest(template = "empty")
    public static void vanillaSmithingStillWorks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        SmithingMenu menu = new SmithingMenu(0, player.getInventory());
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(1).set(new ItemStack(Items.NETHERITE_INGOT));
        check(helper, menu.getSlot(2).getItem().getItem() == Items.NETHERITE_SWORD, "Original two-input netherite recipe must work");
        check(helper, menu.quickMoveStack(player, 2).getItem() == Items.NETHERITE_SWORD, "Vanilla output must be collectible");
        check(helper, menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "Vanilla inputs must be consumed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void swordReachFollowsMainHand(GameTestHelper helper) {
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

    private static void verifySwordReach(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        double baseline = player.getAttributeValue(ForgeMod.ATTACK_RANGE.get());
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
        player.tick();
        check(helper, Math.abs(player.getAttributeValue(ForgeMod.ATTACK_RANGE.get())
                - baseline - DragonLootConfig.dragonSwordEntityReachBonus()) < 0.0001D, "Holding dragon sword must add configured reach");
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.tick();
        check(helper, player.getAttributeValue(ForgeMod.ATTACK_RANGE.get()) == baseline, "Unequipping sword must restore reach");
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.DRAGON_SWORD.get()));
        player.tick();
        check(helper, player.getAttributeValue(ForgeMod.ATTACK_RANGE.get()) == baseline, "Offhand sword must not add reach");
    }

    @GameTest(template = "empty")
    public static void shieldCooldownUsesConfiguredMultiplier(GameTestHelper helper) {
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

    private static void verifyShieldCooldown(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
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

    @GameTest(template = "empty")
    public static void tridentThrownDamageIncludesDragonBonus(GameTestHelper helper) {
        LivingEntity target = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        float before = target.getHealth();
        TestTrident trident = new TestTrident(helper.getLevel(), helper.makeMockPlayer());
        trident.hit(target);
        double expected = 8.0D + DragonLootConfig.itemBaseDamage() / 5.0D;
        check(helper, Math.abs(before - target.getHealth() - expected) < 0.0001D, "Thrown trident must apply dragon damage bonus");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void endCityLootHasNoDragonUpgradePool(GameTestHelper helper) {
        var loot = helper.getLevel().getServer().getLootTables().get(new ResourceLocation("minecraft", "chests/end_city_treasure"));
        check(helper, loot.getPool("dragonloot_dragon_upgrade_template") == null, "End City loot must use its vanilla pools");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static class TestTrident extends DragonTridentEntity {
        private TestTrident(Level level, LivingEntity owner) {
            super(level, owner, new ItemStack(ModItems.DRAGON_TRIDENT.get()));
        }

        private void hit(LivingEntity target) {
            onHitEntity(new EntityHitResult(target));
        }
    }
}
