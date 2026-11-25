package net.dragonloot.mixin;

import net.dragonloot.item.DragonCrossbowItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
abstract class CrossbowItemMixin {
    @Inject(method = "getArrow", at = @At("RETURN"))
    private static void dragonloot$boostDragonBolts(Level level, LivingEntity shooter, ItemStack crossbow,
                                                   ItemStack ammunition, CallbackInfoReturnable<AbstractArrow> callback) {
        if (crossbow.getItem() instanceof DragonCrossbowItem) {
            AbstractArrow arrow = callback.getReturnValue();
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        }
    }
}
