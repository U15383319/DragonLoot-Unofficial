package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.block.DragonAnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, DragonLoot.MOD_ID);

    public static final DeferredHolder<Block, DragonAnvilBlock> DRAGON_ANVIL = BLOCKS.register("dragon_anvil",
            () -> new DragonAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)));

    private ModBlocks() {
    }
}
