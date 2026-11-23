package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.dragonloot.config.DragonLootConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class DragonSwordItem extends SwordItem {
    public DragonSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(createAttributes(tier, attackDamage, attackSpeed).withModifierAdded(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(DragonLoot.id("dragon_sword_reach"),
                        DragonLootConfig.dragonSwordEntityReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)));
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
