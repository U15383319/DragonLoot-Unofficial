package net.dragonloot.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @WrapOperation(method = "blockUsingShield", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;disableShield()V"))
    private void dragonloot$extendShieldCooldown(Player defender, Operation<Void> disableShield,
                                                 @Local(argsOnly = true) LivingEntity attacker) {
        int bonus = DragonLootConfig.dragonAxeShieldDisableBonusTicks();
        ItemStack blockingStack = defender.getUseItem();
        if (bonus == 0 || defender.level().isClientSide() || blockingStack.isEmpty()
                || !attacker.getMainHandItem().is(ModItems.DRAGON_AXE.get())) {
            disableShield.call(defender);
            return;
        }

        Item shield = blockingStack.getItem();
        ItemCooldowns cooldowns = defender.getCooldowns();
        var previous = cooldowns.cooldowns.get(shield);
        disableShield.call(defender);
        var current = cooldowns.cooldowns.get(shield);
        if (current != null && current != previous) {
            int remaining = current.endTime - cooldowns.tickCount;
            if (remaining > 0) {
                int extended = (int) Math.min((long) remaining + bonus, Integer.MAX_VALUE);
                cooldowns.addCooldown(shield, extended);
            }
        }
    }
}
