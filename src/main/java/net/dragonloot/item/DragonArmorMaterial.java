package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum DragonArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] BASE_DURABILITY = new int[]{28, 32, 35, 26};

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getIndex()] * DragonLootConfig.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> DragonLootConfig.helmetProtection();
            case CHEST -> DragonLootConfig.chestProtection();
            case LEGS -> DragonLootConfig.leggingsProtection();
            case FEET -> DragonLootConfig.bootsProtection();
            default -> 0;
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
