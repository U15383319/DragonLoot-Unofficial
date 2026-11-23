package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class DragonCrossbowItem extends CrossbowItem {
    public DragonCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity,
                                   float inaccuracy, float angle, @Nullable LivingEntity target) {
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        }
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
