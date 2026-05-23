package net.dragonloot.menu;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DragonAnvilMenu extends AnvilMenu {
    public DragonAnvilMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
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
    protected boolean mayPickup(Player player, boolean hasOutput) {
        return hasOutput && getCost() > 0 && (player.getAbilities().instabuild || player.experienceLevel >= getCost());
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
    }
}
