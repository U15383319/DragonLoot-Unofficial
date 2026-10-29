package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BowItem;

public class DragonBowItem extends BowItem {
    public DragonBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrowEntity customArrow(AbstractArrowEntity arrow) {
        arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        return arrow;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
