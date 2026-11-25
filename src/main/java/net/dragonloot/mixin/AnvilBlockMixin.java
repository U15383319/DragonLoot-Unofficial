package net.dragonloot.mixin;

import net.dragonloot.block.DragonAnvilBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
abstract class AnvilBlockMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private static void dragonloot$preserveDragonAnvil(BlockState state, CallbackInfoReturnable<BlockState> callback) {
        if (state.getBlock() instanceof DragonAnvilBlock) {
            callback.setReturnValue(state);
        }
    }
}
