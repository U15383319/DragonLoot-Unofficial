package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.world.item.Tier;
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
    public int getLevel() {
        return 5;
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
