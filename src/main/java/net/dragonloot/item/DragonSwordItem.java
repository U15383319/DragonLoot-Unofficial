package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dragonloot.config.DragonLootConfig;
import net.minecraft.util.DamageSource;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.IItemTier;
import net.dragonloot.init.ModAttributes;

import java.util.UUID;

public class DragonSwordItem extends SwordItem {
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("a40909e2-7f99-4f07-bcb8-44a7d81ac4fe");

    public DragonSwordItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot != EquipmentSlotType.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(slot, stack));
        builder.put(ModAttributes.ATTACK_RANGE_BONUS.get(), new AttributeModifier(ENTITY_REACH_MODIFIER_UUID, "Dragon sword reach",
                DragonLootConfig.dragonSwordEntityReachBonus(), AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
