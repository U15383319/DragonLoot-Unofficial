package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;

public class DragonMaceItem extends MaceItem {
    private static final double VANILLA_ATTACK_DAMAGE = 5.0D;

    public DragonMaceItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, createToolProperties())
                .attributes(createAttributes()
                        .withModifierAdded(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                                        VANILLA_ATTACK_DAMAGE + DragonLootConfig.itemBaseDamage() / 5.0D,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .withModifierAdded(Attributes.KNOCKBACK_RESISTANCE,
                                new AttributeModifier(DragonLoot.id("dragon_mace_knockback_resistance"),
                                        DragonLootConfig.dragonMaceKnockbackResistance(), AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)));
    }

    @Override
    public int getEnchantmentValue() {
        return DragonLootConfig.toolEnchantability();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(ModItems.DRAGON_SCALE.get());
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
