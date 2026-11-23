package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class DragonPickaxeItem extends PickaxeItem {
    public DragonPickaxeItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(PickaxeItem.createAttributes(tier, attackDamage, attackSpeed)));
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
