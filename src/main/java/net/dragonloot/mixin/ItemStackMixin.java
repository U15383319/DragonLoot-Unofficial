package net.dragonloot.mixin;

import java.util.function.Consumer;
import net.dragonloot.init.ModItems;
import net.dragonloot.item.DragonArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @ModifyVariable(method = "hurtAndBreak", at = @At("HEAD"), argsOnly = true)
    private Consumer<LivingEntity> dragonloot$keepBrokenElytra(Consumer<LivingEntity> onBroken) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() != ModItems.UPGRADED_DRAGON_CHESTPLATE.get()) {
            return onBroken;
        }

        return wearer -> {
            onBroken.accept(wearer);
            if (wearer.getItemBySlot(EquipmentSlot.CHEST) == stack) {
                DragonArmorItem.recoverBrokenElytra(wearer);
            }
        };
    }
}
