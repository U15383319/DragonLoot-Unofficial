package net.dragonloot.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.constants.RecipeTypes;
import net.dragonloot.DragonLoot;
import net.dragonloot.client.screen.DragonSmithingScreen;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class DragonLootJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return DragonLoot.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DragonSmithingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            registration.addRecipes(DragonSmithingCategory.TYPE,
                    level.getRecipeManager().getAllRecipesFor(ModRecipes.DRAGON_SMITHING.get()));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), DragonSmithingCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(DragonSmithingMenu.class,
                DragonSmithingCategory.TYPE, 0, 3, 4, 36);
        registration.addRecipeTransferHandler(DragonSmithingMenu.class,
                RecipeTypes.SMITHING, 1, 2, 4, 36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DragonSmithingScreen.class, 99, 45, 28, 21,
                DragonSmithingCategory.TYPE, RecipeTypes.SMITHING);
    }
}
