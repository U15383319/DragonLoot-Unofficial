package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.entity.DragonTridentEntity;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.DRAGON_SMITHING.get());
        check(helper, recipes.size() == 16, "All 16 dragon upgrade recipes must load");
        for (DragonSmithingRecipe recipe : recipes) {
            ItemStack base = recipe.getBase().getItems()[0].copy();
            base.setHoverName(new TextComponent("Port verification"));
            var input = new SimpleContainer(recipe.getTemplate().getItems()[0].copy(), base,
                    recipe.getAddition().getItems()[0].copy());
            check(helper, recipe.matches(input, helper.getLevel()), "Upgrade recipe must match: " + recipe.getId());
            ItemStack output = recipe.assemble(input);
            check(helper, output.is(recipe.getResultItem().getItem()) && output.hasCustomHoverName(),
                    "Upgrade must retain base NBT: " + recipe.getId());
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                var serializer = new DragonSmithingRecipe.Serializer();
                serializer.toNetwork(buffer, recipe);
                var received = serializer.fromNetwork(recipe.getId(), buffer);
                check(helper, received.matches(input, helper.getLevel())
                        && ItemStack.isSameItemSameTags(received.assemble(input), output), "Recipe must sync to clients");
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void smithingConsumesInputsAndPreservesEnchantments(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        DragonSmithingMenu menu = new DragonSmithingMenu(0, player.getInventory());
        ItemStack base = new ItemStack(Items.NETHERITE_SWORD);
        base.enchant(Enchantments.SHARPNESS, 4);
        menu.getSlot(0).set(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get(), 2));
        menu.getSlot(1).set(base);
        menu.getSlot(2).set(new ItemStack(ModItems.DRAGON_SCALE.get(), 2));
        check(helper, menu.getSlot(3).getItem().is(ModItems.DRAGON_SWORD.get()), "Template smithing must produce dragon sword");
        ItemStack taken = menu.quickMoveStack(player, 3);
        check(helper, taken.is(ModItems.DRAGON_SWORD.get()) && taken.isEnchanted(), "Shift-click must preserve enchantments");
        check(helper, menu.getSlot(0).getItem().getCount() == 1 && menu.getSlot(1).getItem().isEmpty()
                && menu.getSlot(2).getItem().getCount() == 1, "One template, base and scale must be consumed");
        check(helper, menu.getSlot(3).getItem().isEmpty(), "Taking output must not duplicate it");
        menu.getSlot(1).set(new ItemStack(Items.NETHERITE_SWORD));
        menu.getSlot(0).set(ItemStack.EMPTY);
        check(helper, menu.getSlot(3).getItem().isEmpty(), "Dragon upgrade must require a template");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vanillaSmithingStillWorks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        DragonSmithingMenu menu = new DragonSmithingMenu(0, player.getInventory());
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(2).set(new ItemStack(Items.NETHERITE_INGOT));
        check(helper, menu.getSlot(3).getItem().is(Items.NETHERITE_SWORD), "Original two-input netherite recipe must work");
        check(helper, menu.quickMoveStack(player, 3).is(Items.NETHERITE_SWORD), "Vanilla output must be collectible");
        check(helper, menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty(), "Vanilla inputs must be consumed");
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
    public static void templateDuplicationAndEndCityLoot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        CraftingContainer grid = new CraftingContainer(new DragonSmithingMenu(0, player.getInventory()), 3, 3);
        for (int i = 0; i < 9; i++) {
            grid.setItem(i, new ItemStack(i == 1 ? ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()
                    : i == 4 ? Items.PURPUR_BLOCK : Items.AMETHYST_SHARD));
        }
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        check(helper, recipe.isPresent() && recipe.get().assemble(grid).is(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get())
                && recipe.get().assemble(grid).getCount() == 2, "Template duplication recipe must return two templates");
        var loot = helper.getLevel().getServer().getLootTables().get(new ResourceLocation("minecraft", "chests/end_city_treasure"));
        check(helper, loot.getPool("dragonloot_dragon_upgrade_template") != null, "End City chest loot must include the template pool");
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
