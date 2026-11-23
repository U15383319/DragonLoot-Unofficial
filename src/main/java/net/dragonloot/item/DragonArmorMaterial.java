package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

public final class DragonArmorMaterial {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, DragonLoot.MOD_ID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DRAGON = MATERIALS.register("dragon",
            () -> new ArmorMaterial(Map.of(
                    ArmorItem.Type.HELMET, DragonLootConfig.helmetProtection(),
                    ArmorItem.Type.CHESTPLATE, DragonLootConfig.chestProtection(),
                    ArmorItem.Type.LEGGINGS, DragonLootConfig.leggingsProtection(),
                    ArmorItem.Type.BOOTS, DragonLootConfig.bootsProtection(),
                    ArmorItem.Type.BODY, 18),
                    DragonLootConfig.armorEnchantability(),
                    SoundEvents.ARMOR_EQUIP_CHAIN,
                    () -> Ingredient.of(ModItems.DRAGON_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(DragonLoot.id("dragon"))),
                    DragonLootConfig.armorToughness(),
                    DragonLootConfig.armorKnockbackResistance() / 10.0F));

    private DragonArmorMaterial() {
    }

    public static int durability(ArmorItem.Type type) {
        int base = switch (type) {
            case BOOTS -> 28;
            case LEGGINGS -> 32;
            case CHESTPLATE -> 35;
            case HELMET -> 26;
            case BODY -> throw new IllegalArgumentException("Horse armor is not damageable");
        };
        return base * DragonLootConfig.armorDurabilityMultiplier();
    }
}
