package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;

public class DragonShovelItem extends ShovelItem {
    public DragonShovelItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
