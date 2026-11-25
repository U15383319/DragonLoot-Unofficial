package net.dragonloot.config;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.IOException;
import java.nio.file.Files;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;

public final class DragonLootConfig {
    public static final String FILE_NAME = "dragonloot-common.toml";
    public static final ForgeConfigSpec SPEC;

    private static final int DEFAULT_SCALE_MINIMUM_DROP_AMOUNT = 3;
    private static final int DEFAULT_ADDITIONAL_SCALES_PER_PLAYER = 2;
    private static final double DEFAULT_ADDITIONAL_SCALE_DROP_CHANCE = 0.8D;
    private static final int DEFAULT_DRAGON_ARMOR_PROTECTION_HELMET = 7;
    private static final int DEFAULT_DRAGON_ARMOR_PROTECTION_CHEST = 10;
    private static final int DEFAULT_DRAGON_ARMOR_PROTECTION_LEGGINGS = 9;
    private static final int DEFAULT_DRAGON_ARMOR_PROTECTION_BOOTS = 7;
    private static final double DEFAULT_DRAGON_ARMOR_TOUGHNESS = 3.0D;
    private static final double DEFAULT_DRAGON_ARMOR_KNOCKBACK_RESISTANCE = 1.0D;
    private static final int DEFAULT_DRAGON_ARMOR_ENCHANTABILITY = 15;
    private static final int DEFAULT_DRAGON_ARMOR_DURABILITY_MULTIPLIER = 37;
    private static final int DEFAULT_DRAGON_ITEM_DURABILITY_MULTIPLIER = 37;
    private static final double DEFAULT_DRAGON_ITEM_BASE_DAMAGE = 5.0D;
    private static final int DEFAULT_DRAGON_TOOL_ENCHANTABILITY = 20;
    private static final boolean DEFAULT_DRAGON_ANVIL_NO_CAP = true;
    private static final double DEFAULT_DRAGON_ANVIL_COST_MULTIPLIER = 0.8D;
    private static final int DEFAULT_DRAGON_ANVIL_MAX_COST = 30;
    private static final double DEFAULT_DRAGON_SHIELD_DISABLE_COOLDOWN_MULTIPLIER = 0.4D;
    private static final double DEFAULT_DRAGON_SWORD_ENTITY_REACH_BONUS = 0.5D;
    private static final int DEFAULT_DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS = 20;

    private static final ForgeConfigSpec.IntValue SCALE_MINIMUM_DROP_AMOUNT;
    private static final ForgeConfigSpec.IntValue ADDITIONAL_SCALES_PER_PLAYER;
    private static final ForgeConfigSpec.DoubleValue ADDITIONAL_SCALE_DROP_CHANCE;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_PROTECTION_HELMET;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_PROTECTION_CHEST;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_PROTECTION_LEGGINGS;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_PROTECTION_BOOTS;
    private static final ForgeConfigSpec.DoubleValue DRAGON_ARMOR_TOUGHNESS;
    private static final ForgeConfigSpec.DoubleValue DRAGON_ARMOR_KNOCKBACK_RESISTANCE;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_ENCHANTABILITY;
    private static final ForgeConfigSpec.IntValue DRAGON_ARMOR_DURABILITY_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue DRAGON_ITEM_DURABILITY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue DRAGON_ITEM_BASE_DAMAGE;
    private static final ForgeConfigSpec.IntValue DRAGON_TOOL_ENCHANTABILITY;
    private static final ForgeConfigSpec.BooleanValue DRAGON_ANVIL_NO_CAP;
    private static final ForgeConfigSpec.DoubleValue DRAGON_ANVIL_COST_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue DRAGON_ANVIL_MAX_COST;
    private static final ForgeConfigSpec.DoubleValue DRAGON_SHIELD_DISABLE_COOLDOWN_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue DRAGON_SWORD_ENTITY_REACH_BONUS;
    private static final ForgeConfigSpec.IntValue DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SCALE_MINIMUM_DROP_AMOUNT = builder.defineInRange("scale_minimum_drop_amount", 3, 0, 64);
        ADDITIONAL_SCALES_PER_PLAYER = builder.defineInRange("additional_scales_per_player", 2, 0, 64);
        ADDITIONAL_SCALE_DROP_CHANCE = builder.comment("0.8 = 80%").defineInRange("additional_scale_drop_chance", 0.8D, 0.0D, 1.0D);
        DRAGON_ARMOR_PROTECTION_HELMET = builder.worldRestart().defineInRange("dragon_armor_protection_helmet", 7, 0, 20);
        DRAGON_ARMOR_PROTECTION_CHEST = builder.worldRestart().defineInRange("dragon_armor_protection_chest", 10, 0, 20);
        DRAGON_ARMOR_PROTECTION_LEGGINGS = builder.worldRestart().defineInRange("dragon_armor_protection_leggings", 9, 0, 20);
        DRAGON_ARMOR_PROTECTION_BOOTS = builder.worldRestart().defineInRange("dragon_armor_protection_boots", 7, 0, 20);
        DRAGON_ARMOR_TOUGHNESS = builder.worldRestart().defineInRange("dragon_armor_toughness", 3.0D, 0.0D, 20.0D);
        DRAGON_ARMOR_KNOCKBACK_RESISTANCE = builder.worldRestart().defineInRange("dragon_armor_knockback_resistance", 1.0D, 0.0D, 20.0D);
        DRAGON_ARMOR_ENCHANTABILITY = builder.worldRestart().defineInRange("dragon_armor_enchantability", 15, 0, 100);
        DRAGON_ARMOR_DURABILITY_MULTIPLIER = builder.worldRestart().defineInRange("dragon_armor_durability_multiplier", 37, 1, 1000);
        DRAGON_ITEM_DURABILITY_MULTIPLIER = builder.worldRestart().defineInRange("dragon_item_durability_multiplier", 37, 1, 1000);
        DRAGON_ITEM_BASE_DAMAGE = builder.worldRestart().defineInRange("dragon_item_base_damage", 5.0D, 0.0D, 100.0D);
        DRAGON_TOOL_ENCHANTABILITY = builder.worldRestart().defineInRange("dragon_tool_enchantability", 20, 0, 100);
        DRAGON_ANVIL_NO_CAP = builder
                .comment("Enable the dragon anvil discount and replace vanilla's Too Expensive rule.")
                .define("dragon_anvil_no_cap", DEFAULT_DRAGON_ANVIL_NO_CAP);
        DRAGON_ANVIL_COST_MULTIPLIER = builder
                .comment("Fraction of the original level cost. Rounded up, with a minimum cost of 1 level.")
                .defineInRange("dragon_anvil_cost_multiplier", DEFAULT_DRAGON_ANVIL_COST_MULTIPLIER, 0.0D, 1.0D);
        DRAGON_ANVIL_MAX_COST = builder
                .comment("Maximum number of levels the dragon anvil charges. Menu costs are sent as signed shorts.")
                .defineInRange("dragon_anvil_max_cost", DEFAULT_DRAGON_ANVIL_MAX_COST, 1, Short.MAX_VALUE);
        DRAGON_SHIELD_DISABLE_COOLDOWN_MULTIPLIER = builder
                .comment("Multiplier applied to dragon shield disable cooldown. 0.4 = 40% of the original cooldown.")
                .defineInRange("dragon_shield_disable_cooldown_multiplier", 0.4D, 0.0D, 1.0D);
        DRAGON_SWORD_ENTITY_REACH_BONUS = builder
                .comment("Additional entity attack reach granted while the dragon sword is in the main hand.")
                .defineInRange("dragon_sword_entity_reach_bonus", 0.5D, 0.0D, 16.0D);
        DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS = builder
                .comment("Extra cooldown ticks added when the dragon axe disables a shield. 20 ticks = 1 second.")
                .defineInRange("dragon_axe_shield_disable_bonus_ticks", DEFAULT_DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS, 0, 1200);
        SPEC = builder.build();
    }

    private DragonLootConfig() {
    }

    public static void loadBeforeRegistration() {
        // Forge loads COMMON configs after registries, but item constructors already need these values.
        var path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.isRegularFile(path)) {
            return;
        }

        try (var reader = Files.newBufferedReader(path)) {
            SPEC.setConfig(new TomlParser().parse(reader));
        } catch (IOException | ParsingException error) {
            LogManager.getLogger(DragonLootConfig.class).warn("Could not read startup values from {}", path, error);
        }
    }

    public static int scaleMinimumDropAmount() { return getOrDefault(SCALE_MINIMUM_DROP_AMOUNT, DEFAULT_SCALE_MINIMUM_DROP_AMOUNT); }
    public static int additionalScalesPerPlayer() { return getOrDefault(ADDITIONAL_SCALES_PER_PLAYER, DEFAULT_ADDITIONAL_SCALES_PER_PLAYER); }
    public static float additionalScaleDropChance() { return getOrDefault(ADDITIONAL_SCALE_DROP_CHANCE, DEFAULT_ADDITIONAL_SCALE_DROP_CHANCE).floatValue(); }
    public static int helmetProtection() { return getOrDefault(DRAGON_ARMOR_PROTECTION_HELMET, DEFAULT_DRAGON_ARMOR_PROTECTION_HELMET); }
    public static int chestProtection() { return getOrDefault(DRAGON_ARMOR_PROTECTION_CHEST, DEFAULT_DRAGON_ARMOR_PROTECTION_CHEST); }
    public static int leggingsProtection() { return getOrDefault(DRAGON_ARMOR_PROTECTION_LEGGINGS, DEFAULT_DRAGON_ARMOR_PROTECTION_LEGGINGS); }
    public static int bootsProtection() { return getOrDefault(DRAGON_ARMOR_PROTECTION_BOOTS, DEFAULT_DRAGON_ARMOR_PROTECTION_BOOTS); }
    public static float armorToughness() { return getOrDefault(DRAGON_ARMOR_TOUGHNESS, DEFAULT_DRAGON_ARMOR_TOUGHNESS).floatValue(); }
    public static float armorKnockbackResistance() { return getOrDefault(DRAGON_ARMOR_KNOCKBACK_RESISTANCE, DEFAULT_DRAGON_ARMOR_KNOCKBACK_RESISTANCE).floatValue(); }
    public static int armorEnchantability() { return getOrDefault(DRAGON_ARMOR_ENCHANTABILITY, DEFAULT_DRAGON_ARMOR_ENCHANTABILITY); }
    public static int armorDurabilityMultiplier() { return getOrDefault(DRAGON_ARMOR_DURABILITY_MULTIPLIER, DEFAULT_DRAGON_ARMOR_DURABILITY_MULTIPLIER); }
    public static int itemDurabilityMultiplier() { return getOrDefault(DRAGON_ITEM_DURABILITY_MULTIPLIER, DEFAULT_DRAGON_ITEM_DURABILITY_MULTIPLIER); }
    public static float itemBaseDamage() { return getOrDefault(DRAGON_ITEM_BASE_DAMAGE, DEFAULT_DRAGON_ITEM_BASE_DAMAGE).floatValue(); }
    public static int toolEnchantability() { return getOrDefault(DRAGON_TOOL_ENCHANTABILITY, DEFAULT_DRAGON_TOOL_ENCHANTABILITY); }
    public static boolean dragonAnvilNoCap() { return getOrDefault(DRAGON_ANVIL_NO_CAP, DEFAULT_DRAGON_ANVIL_NO_CAP); }
    public static double dragonAnvilCostMultiplier() { return getOrDefault(DRAGON_ANVIL_COST_MULTIPLIER, DEFAULT_DRAGON_ANVIL_COST_MULTIPLIER); }
    public static int dragonAnvilMaxCost() { return getOrDefault(DRAGON_ANVIL_MAX_COST, DEFAULT_DRAGON_ANVIL_MAX_COST); }
    public static double dragonShieldDisableCooldownMultiplier() { return getOrDefault(DRAGON_SHIELD_DISABLE_COOLDOWN_MULTIPLIER, DEFAULT_DRAGON_SHIELD_DISABLE_COOLDOWN_MULTIPLIER); }
    public static double dragonSwordEntityReachBonus() { return getOrDefault(DRAGON_SWORD_ENTITY_REACH_BONUS, DEFAULT_DRAGON_SWORD_ENTITY_REACH_BONUS); }
    public static int dragonAxeShieldDisableBonusTicks() { return getOrDefault(DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS, DEFAULT_DRAGON_AXE_SHIELD_DISABLE_BONUS_TICKS); }

    private static <T> T getOrDefault(ForgeConfigSpec.ConfigValue<T> value, T fallback) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }
}
