package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;

public class DragonScaleItem extends Item {
    public DragonScaleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
