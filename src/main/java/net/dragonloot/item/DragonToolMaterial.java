package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.Ingredient;

public enum DragonToolMaterial implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 67 * DragonLootConfig.itemDurabilityMultiplier();
    }

    @Override
    public float getSpeed() {
        return 12.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return DragonLootConfig.itemBaseDamage();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return DragonLootConfig.toolEnchantability();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.DRAGON_SCALE.get());
    }

    @Override
    public String toString() {
        return "DRAGON";
    }
}
