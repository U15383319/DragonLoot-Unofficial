package net.dragonloot.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dragonloot.DragonLoot;
import net.dragonloot.init.ModItems;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;

public class DragonSmithingCategory implements IRecipeCategory<DragonSmithingRecipe> {
    public static final ResourceLocation TYPE = DragonLoot.id("smithing_transform");
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public DragonSmithingCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(130, 24);
        this.icon = gui.createDrawableIngredient(new ItemStack(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()));
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.createDrawable(new ResourceLocation("minecraft", "textures/gui/container/smithing.png"), 102, 48, 22, 15);
    }

    @Override
    public ResourceLocation getUid() {
        return TYPE;
    }

    @Override
    public Class<? extends DragonSmithingRecipe> getRecipeClass() {
        return DragonSmithingRecipe.class;
    }

    @Override
    public String getTitle() {
        return new TranslationTextComponent("upgrade.dragonloot.dragon_upgrade").getString();
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
    public void setIngredients(DragonSmithingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(java.util.Arrays.asList(recipe.getTemplate(), recipe.getBase(), recipe.getAddition()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
    }

    @Override
    public void setRecipe(IRecipeLayout layout, DragonSmithingRecipe recipe, IIngredients ingredients) {
        var stacks = layout.getItemStacks();
        stacks.init(0, true, 0, 3);
        stacks.init(1, true, 26, 3);
        stacks.init(2, true, 52, 3);
        stacks.init(3, false, 110, 3);
        stacks.set(ingredients);
    }

    @Override
    public void draw(DragonSmithingRecipe recipe, MatrixStack poseStack, double mouseX, double mouseY) {
        for (int x : new int[]{0, 26, 52, 110}) {
            slot.draw(poseStack, x, 3);
        }
        arrow.draw(poseStack, 79, 4);
    }
}
