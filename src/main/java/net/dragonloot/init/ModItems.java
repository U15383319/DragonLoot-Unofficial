package net.dragonloot.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.dragonloot.DragonLoot;
import net.dragonloot.item.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DragonLoot.MOD_ID);

    public static final RegistryObject<Item> DRAGON_SCALE = ITEMS.register("dragon_scale",
            () -> new DragonScaleItem(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("dragon_upgrade_smithing_template",
            ModItems::createDragonUpgradeTemplate);
    public static final RegistryObject<Item> DRAGON_ANVIL = ITEMS.register("dragon_anvil",
            () -> new BlockItem(ModBlocks.DRAGON_ANVIL.get(), new Item.Properties()));
    public static final RegistryObject<Item> DRAGON_HORSE_ARMOR = ITEMS.register("dragon_horse_armor",
            () -> new DragonHorseArmorItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> DRAGON_HELMET = ITEMS.register("dragon_helmet",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_CHESTPLATE = ITEMS.register("dragon_chestplate",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_LEGGINGS = ITEMS.register("dragon_leggings",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_BOOTS = ITEMS.register("dragon_boots",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> UPGRADED_DRAGON_CHESTPLATE = ITEMS.register("upgraded_dragon_chestplate",
            () -> new DragonArmorItem(DragonArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant(), true));

    public static final RegistryObject<Item> DRAGON_PICKAXE = ITEMS.register("dragon_pickaxe",
            () -> new DragonPickaxeItem(DragonToolMaterial.INSTANCE, 1, -2.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_AXE = ITEMS.register("dragon_axe",
            () -> new DragonAxeItem(DragonToolMaterial.INSTANCE, 5.0F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_SHOVEL = ITEMS.register("dragon_shovel",
            () -> new DragonShovelItem(DragonToolMaterial.INSTANCE, 1.5F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_HOE = ITEMS.register("dragon_hoe",
            () -> new DragonHoeItem(DragonToolMaterial.INSTANCE, -4, -2.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_SWORD = ITEMS.register("dragon_sword",
            () -> new DragonSwordItem(DragonToolMaterial.INSTANCE, 3, -2.4F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> DRAGON_BOW = ITEMS.register("dragon_bow",
            () -> new DragonBowItem(new Item.Properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));
    public static final RegistryObject<Item> DRAGON_CROSSBOW = ITEMS.register("dragon_crossbow",
            () -> new DragonCrossbowItem(new Item.Properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));
    public static final RegistryObject<Item> DRAGON_TRIDENT = ITEMS.register("dragon_trident",
            () -> new DragonTridentItem(new Item.Properties().durability(DragonToolMaterial.INSTANCE.getUses()).fireResistant()));

    private ModItems() {
    }

    private static SmithingTemplateItem createDragonUpgradeTemplate() {
        return new SmithingTemplateItem(
                Component.translatable("item.dragonloot.smithing_template.dragon_upgrade.applies_to").withStyle(ChatFormatting.BLUE),
                Component.translatable("item.dragonloot.smithing_template.dragon_upgrade.ingredients").withStyle(ChatFormatting.BLUE),
                Component.translatable("upgrade.dragonloot.dragon_upgrade").withStyle(ChatFormatting.GRAY),
                Component.translatable("item.dragonloot.smithing_template.dragon_upgrade.base_slot_description"),
                Component.translatable("item.dragonloot.smithing_template.dragon_upgrade.additions_slot_description"),
                List.of(
                        new ResourceLocation("item/empty_armor_slot_helmet"),
                        new ResourceLocation("item/empty_slot_sword"),
                        new ResourceLocation("item/empty_armor_slot_chestplate"),
                        new ResourceLocation("item/empty_slot_pickaxe"),
                        new ResourceLocation("item/empty_armor_slot_leggings"),
                        new ResourceLocation("item/empty_slot_axe"),
                        new ResourceLocation("item/empty_armor_slot_boots"),
                        new ResourceLocation("item/empty_slot_hoe"),
                        new ResourceLocation("item/empty_slot_shovel")
                ),
                List.of(DragonLoot.id("item/dragon_scale"))
        );
    }
}
