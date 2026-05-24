package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class DragonSwordItem extends SwordItem {
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("a40909e2-7f99-4f07-bcb8-44a7d81ac4fe");
    public static final double ENTITY_REACH_BONUS = 0.5D;

    public DragonSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(slot, stack));
        builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ENTITY_REACH_MODIFIER_UUID, "Dragon sword reach",
                ENTITY_REACH_BONUS, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(source);
    }
}
