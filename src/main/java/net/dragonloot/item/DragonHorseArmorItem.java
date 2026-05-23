package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.HorseArmorItem;

public class DragonHorseArmorItem extends HorseArmorItem {
    public DragonHorseArmorItem(Properties properties) {
        super(18, "dragon", properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(source);
    }
}
