package net.dragonloot.mixin;

import net.dragonloot.item.DragonCrossbowItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
abstract class CrossbowItemMixin {
    @Inject(method = "getArrow", at = @At("RETURN"))
    private static void dragonloot$boostDragonBolts(World level, LivingEntity shooter, ItemStack crossbow,
                                                   ItemStack ammunition, CallbackInfoReturnable<AbstractArrowEntity> callback) {
        if (crossbow.getItem() instanceof DragonCrossbowItem) {
            AbstractArrowEntity arrow = callback.getReturnValue();
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        }
    }
}
