package net.dragonloot.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dragonloot.DragonLoot;
import net.dragonloot.init.ModItems;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DragonSmithingCategory implements IRecipeCategory<DragonSmithingRecipe> {
    public static final RecipeType<DragonSmithingRecipe> TYPE =
            new RecipeType<>(DragonLoot.id("smithing_transform"), DragonSmithingRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public DragonSmithingCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(130, 24);
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()));
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.createDrawable(new ResourceLocation("minecraft", "textures/gui/container/smithing.png"), 102, 48, 22, 15);
    }

    @Override
    public RecipeType<DragonSmithingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getUid() {
        return TYPE.getUid();
    }

    @Override
    public Class<? extends DragonSmithingRecipe> getRecipeClass() {
        return DragonSmithingRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("upgrade.dragonloot.dragon_upgrade");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DragonSmithingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 4).setBackground(slot, -1, -1).addIngredients(recipe.getTemplate());
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 4).setBackground(slot, -1, -1).addIngredients(recipe.getBase());
        builder.addSlot(RecipeIngredientRole.INPUT, 53, 4).setBackground(slot, -1, -1).addIngredients(recipe.getAddition());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 4).setBackground(slot, -1, -1).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(DragonSmithingRecipe recipe, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        arrow.draw(poseStack, 79, 4);
    }
}
