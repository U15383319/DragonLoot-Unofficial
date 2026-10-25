package net.dragonloot.menu;

import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class DragonSmithingMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Level level;
    private final ResultContainer result = new ResultContainer();
    private final Container input = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            DragonSmithingMenu.this.slotsChanged(this);
        }
    };
    private Recipe<Container> selectedRecipe;

    public DragonSmithingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public DragonSmithingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.DRAGON_SMITHING.get(), id);
        this.access = access;
        this.level = inventory.player.level;
        addSlot(new Slot(input, 0, 26, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get());
            }
        });
        addSlot(new Slot(input, 1, 52, 47));
        addSlot(new Slot(input, 2, 78, 47));
        addSlot(new Slot(result, 0, 134, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return hasItem();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                boolean consumesTemplate = selectedRecipe instanceof DragonSmithingRecipe;
                stack.onCraftedBy(level, player, stack.getCount());
                result.awardUsedRecipes(player);
                if (consumesTemplate) {
                    input.removeItem(0, 1);
                }
                input.removeItem(1, 1);
                input.removeItem(2, 1);
                access.execute((world, pos) -> world.levelEvent(1044, pos, 0));
                super.onTake(player, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (level.isClientSide) {
            return;
        }
        selectedRecipe = null;
        ItemStack output = ItemStack.EMPTY;
        if (!input.getItem(0).isEmpty()) {
            var recipe = level.getRecipeManager().getRecipeFor(ModRecipes.DRAGON_SMITHING.get(), input, level);
            if (recipe.isPresent()) {
                selectedRecipe = recipe.get();
                output = selectedRecipe.assemble(input);
            }
        } else {
            Container vanillaInput = new SimpleContainer(input.getItem(1), input.getItem(2));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, vanillaInput, level);
            if (recipe.isPresent()) {
                selectedRecipe = recipe.get();
                output = selectedRecipe.assemble(vanillaInput);
            }
        }
        result.setRecipeUsed(selectedRecipe);
        result.setItem(0, output);
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 3) {
            if (!moveItemStackTo(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < 3) {
            if (!moveItemStackTo(stack, 4, 40, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get())) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isAddition(stack)) {
            if (!moveItemStackTo(stack, 2, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 1, 2, false)) {
            if (index < 31 ? !moveItemStackTo(stack, 31, 40, false) : !moveItemStackTo(stack, 4, 31, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, index == 3 ? original : stack);
        return original;
    }

    private boolean isAddition(ItemStack stack) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.DRAGON_SMITHING.get()).stream()
                .anyMatch(recipe -> recipe.getAddition().test(stack))
                || level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING).stream()
                .anyMatch(recipe -> recipe.isAdditionIngredient(stack));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Blocks.SMITHING_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((world, pos) -> clearContainer(player, input));
    }
}
