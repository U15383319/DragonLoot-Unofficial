package net.dragonloot.block;

import net.dragonloot.menu.DragonAnvilMenu;
import net.dragonloot.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DragonAnvilBlock extends AnvilBlock {
    public DragonAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState fallingState, BlockState landedOn, net.minecraft.world.entity.item.FallingBlockEntity fallingBlock) {
        level.setBlock(pos, ModBlocks.DRAGON_ANVIL.get().defaultBlockState().setValue(FACING, fallingState.getValue(FACING)), 3);
        super.onLand(level, pos, fallingState, landedOn, fallingBlock);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new DragonAnvilMenu(id, inventory, ContainerLevelAccess.create(level, pos)), new TranslatableComponent("container.repair"));
    }
}
