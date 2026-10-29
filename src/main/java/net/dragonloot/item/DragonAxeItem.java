package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;

public class DragonAxeItem extends AxeItem {
    public DragonAxeItem(IItemTier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
