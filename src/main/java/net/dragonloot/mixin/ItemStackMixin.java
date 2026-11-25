package net.dragonloot.mixin;

import java.util.function.Consumer;
import net.dragonloot.init.ModItems;
import net.dragonloot.item.DragonArmorItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), argsOnly = true)
    private Consumer<Item> dragonloot$keepBrokenElytra(Consumer<Item> onBroken, int amount, ServerLevel level,
                                                       @Nullable LivingEntity wearer, Consumer<Item> original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (wearer == null || stack.getItem() != ModItems.UPGRADED_DRAGON_CHESTPLATE.get()
                || wearer.getItemBySlot(EquipmentSlot.CHEST) != stack) {
            return onBroken;
        }

        return item -> {
            onBroken.accept(item);
            if (wearer.getItemBySlot(EquipmentSlot.CHEST) == stack) {
                DragonArmorItem.recoverBrokenElytra(wearer);
            }
        };
    }
}
