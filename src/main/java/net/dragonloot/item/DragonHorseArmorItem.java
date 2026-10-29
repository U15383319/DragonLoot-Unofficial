package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.item.HorseArmorItem;

public class DragonHorseArmorItem extends HorseArmorItem {
    public DragonHorseArmorItem(Properties properties) {
        super(18, "dragon", properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
