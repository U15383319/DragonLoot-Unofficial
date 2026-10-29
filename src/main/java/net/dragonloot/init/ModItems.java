package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.item.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DragonLoot.MOD_ID);

    public static final RegistryObject<Item> DRAGON_SCALE = ITEMS.register("dragon_scale",
            () -> new DragonScaleItem(properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("dragon_upgrade_smithing_template",
            () -> new DragonUpgradeTemplateItem(properties()));
    public static final RegistryObject<Item> DRAGON_ANVIL = ITEMS.register("dragon_anvil",
            () -> new BlockItem(ModBlocks.DRAGON_ANVIL.get(), properties()));
    public static final RegistryObject<Item> DRAGON_HORSE_ARMOR = ITEMS.register("dragon_horse_armor",
            () -> new DragonHorseArmorItem(properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> DRAGON_HELMET = ITEMS.register("dragon_helmet",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, EquipmentSlotType.HEAD, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_CHESTPLATE = ITEMS.register("dragon_chestplate",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, EquipmentSlotType.CHEST, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_LEGGINGS = ITEMS.register("dragon_leggings",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, EquipmentSlotType.LEGS, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_BOOTS = ITEMS.register("dragon_boots",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, EquipmentSlotType.FEET, properties().fireResistant()));
    public static final RegistryObject<Item> UPGRADED_DRAGON_CHESTPLATE = ITEMS.register("upgraded_dragon_chestplate",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, EquipmentSlotType.CHEST, properties().fireResistant(), true));

    public static final RegistryObject<Item> DRAGON_PICKAXE = ITEMS.register("dragon_pickaxe",
            () -> new DragonPickaxeItem(DragonToolMaterial.INSTANCE, 1, -2.8F, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_AXE = ITEMS.register("dragon_axe",
            () -> new DragonAxeItem(DragonToolMaterial.INSTANCE, 5.0F, -3.0F, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_SHOVEL = ITEMS.register("dragon_shovel",
            () -> new DragonShovelItem(DragonToolMaterial.INSTANCE, 1.5F, -3.0F, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_HOE = ITEMS.register("dragon_hoe",
            () -> new DragonHoeItem(DragonToolMaterial.INSTANCE, -4, -2.0F, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_SWORD = ITEMS.register("dragon_sword",
            () -> new DragonSwordItem(DragonToolMaterial.INSTANCE, 3, -2.4F, properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_BOW = ITEMS.register("dragon_bow",
            () -> new DragonBowItem(properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));
    public static final RegistryObject<Item> DRAGON_CROSSBOW = ITEMS.register("dragon_crossbow",
            () -> new DragonCrossbowItem(properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));
    public static final RegistryObject<Item> DRAGON_TRIDENT = ITEMS.register("dragon_trident",
            () -> new DragonTridentItem(properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));
    public static final RegistryObject<Item> DRAGON_SHIELD = ITEMS.register("dragon_shield",
            () -> new DragonShieldItem(properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));

    private ModItems() {
    }

    private static Item.Properties properties() {
        return new Item.Properties().tab(ModCreativeTabs.DRAGONLOOT);
    }
}
