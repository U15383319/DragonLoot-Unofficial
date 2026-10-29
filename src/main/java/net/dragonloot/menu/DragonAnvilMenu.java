package net.dragonloot.menu;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;

public class DragonAnvilMenu extends RepairContainer {
    public DragonAnvilMenu(int containerId, PlayerInventory inventory, IWorldPosCallable access) {
        super(containerId, inventory, access);
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.DRAGON_ANVIL.get());
    }

    @Override
    public void createResult() {
        super.createResult();
        if (DragonLootConfig.dragonAnvilNoCap() && getCost() > 30) {
            setMaximumCost(30);
        }
    }

    @Override
    protected boolean mayPickup(PlayerEntity player, boolean hasOutput) {
        return hasOutput && getCost() > 0 && (player.abilities.instabuild || player.experienceLevel >= getCost());
    }

    @Override
    protected ItemStack onTake(PlayerEntity player, ItemStack stack) {
        return super.onTake(player, stack);
    }
}
