package net.dragonloot.menu;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DragonAnvilMenu extends AnvilMenu {
    public static final int VANILLA_COST_LIMIT = 40;

    private final DataSlot synchronizedCost;
    private boolean calculatingDiscountedCost;

    public DragonAnvilMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public DragonAnvilMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(containerId, inventory, access);
        synchronizedCost = cost;
        // AnvilMenu registered this original slot for synchronization in its constructor.
        cost = new DataSlot() {
            @Override
            public int get() {
                int price = synchronizedCost.get();
                // createResult reads the slot for the vanilla rename cap and rejection rule.
                return calculatingDiscountedCost ? Math.min(price, VANILLA_COST_LIMIT - 1) : price;
            }

            @Override
            public void set(int originalCost) {
                synchronizedCost.set(calculatingDiscountedCost && originalCost > 0
                        ? discountedCost(originalCost)
                        : originalCost);
            }
        };
    }

    @Override
    public MenuType<?> getType() {
        return ModMenus.DRAGON_ANVIL.get();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.DRAGON_ANVIL.get());
    }

    @Override
    public void createResult() {
        boolean previous = calculatingDiscountedCost;
        calculatingDiscountedCost = DragonLootConfig.dragonAnvilNoCap();
        try {
            super.createResult();
        } finally {
            calculatingDiscountedCost = previous;
        }
    }

    @Override
    public int getCost() {
        return synchronizedCost.get();
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasOutput) {
        return hasOutput && getCost() > 0 && (player.getAbilities().instabuild || player.experienceLevel >= getCost());
    }

    private static int discountedCost(int originalCost) {
        int roundedCost = BigDecimal.valueOf(DragonLootConfig.dragonAnvilCostMultiplier())
                .multiply(BigDecimal.valueOf(originalCost))
                .setScale(0, RoundingMode.CEILING)
                .intValueExact();
        return Math.min(DragonLootConfig.dragonAnvilMaxCost(), Math.max(1, roundedCost));
    }
}
