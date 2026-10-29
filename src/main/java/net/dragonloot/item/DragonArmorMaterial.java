package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;

public enum DragonArmorMaterial implements IArmorMaterial {
    INSTANCE;

    private static final int[] BASE_DURABILITY = new int[]{28, 32, 35, 26};

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slot) {
        return BASE_DURABILITY[slot.getIndex()] * DragonLootConfig.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType slot) {
        switch (slot) {
            case HEAD: return DragonLootConfig.helmetProtection();
            case CHEST: return DragonLootConfig.chestProtection();
            case LEGS: return DragonLootConfig.leggingsProtection();
            case FEET: return DragonLootConfig.bootsProtection();
            default: return 0;
        }
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
