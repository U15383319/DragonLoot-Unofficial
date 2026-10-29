package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.item.HoeItem;
import net.minecraft.item.IItemTier;

public class DragonHoeItem extends HoeItem {
    public DragonHoeItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
