package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.block.DragonAnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DragonLoot.MOD_ID);

    public static final RegistryObject<DragonAnvilBlock> DRAGON_ANVIL = BLOCKS.register("dragon_anvil",
            () -> new DragonAnvilBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL)));

    private ModBlocks() {
    }
}
