package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum DragonArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] BASE_DURABILITY = new int[]{28, 32, 35, 26};

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * DragonLootConfig.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> DragonLootConfig.helmetProtection();
            case CHESTPLATE -> DragonLootConfig.chestProtection();
            case LEGGINGS -> DragonLootConfig.leggingsProtection();
            case BOOTS -> DragonLootConfig.bootsProtection();
        };
    }

    @Override
    public int getEnchantmentValue() {
        return DragonLootConfig.armorEnchantability();
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_CHAIN;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.DRAGON_SCALE.get());
    }

    @Override
    public String getName() {
        return "dragon";
    }

    @Override
    public float getToughness() {
        return DragonLootConfig.armorToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return DragonLootConfig.armorKnockbackResistance();
    }
}
