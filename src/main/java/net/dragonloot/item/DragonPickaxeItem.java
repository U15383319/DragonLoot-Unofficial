package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.IItemTier;

public class DragonPickaxeItem extends PickaxeItem {
    public DragonPickaxeItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
