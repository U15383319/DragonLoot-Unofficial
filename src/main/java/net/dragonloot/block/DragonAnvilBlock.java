package net.dragonloot.block;

import net.dragonloot.menu.DragonAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new DragonAnvilMenu(id, inventory, ContainerLevelAccess.create(level, pos)), Component.translatable("container.repair"));
    }
}
