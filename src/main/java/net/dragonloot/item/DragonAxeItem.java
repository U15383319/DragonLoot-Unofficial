package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

public class DragonAxeItem extends AxeItem {
    public DragonAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(AxeItem.createAttributes(tier, attackDamage, attackSpeed)));
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
