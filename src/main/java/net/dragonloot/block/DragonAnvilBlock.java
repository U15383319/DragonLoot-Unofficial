package net.dragonloot.block;

import net.dragonloot.menu.DragonAnvilMenu;
import net.dragonloot.init.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import javax.annotation.Nullable;

public class DragonAnvilBlock extends AnvilBlock {
    public DragonAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onLand(World level, BlockPos pos, BlockState fallingState, BlockState landedOn, net.minecraft.entity.item.FallingBlockEntity fallingBlock) {
        level.setBlock(pos, ModBlocks.DRAGON_ANVIL.get().defaultBlockState().setValue(FACING, fallingState.getValue(FACING)), 3);
        super.onLand(level, pos, fallingState, landedOn, fallingBlock);
    }

    @Override
    @Nullable
    public INamedContainerProvider getMenuProvider(BlockState state, World level, BlockPos pos) {
        return new SimpleNamedContainerProvider((id, inventory, player) ->
                new DragonAnvilMenu(id, inventory, IWorldPosCallable.create(level, pos)), new TranslationTextComponent("container.repair"));
    }
}
