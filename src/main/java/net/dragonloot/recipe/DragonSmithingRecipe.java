package net.dragonloot.recipe;

import com.google.gson.JsonObject;
import net.dragonloot.init.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

public class DragonSmithingRecipe extends UpgradeRecipe {
    private final Ingredient template;
    private final Ingredient baseIngredient;
    private final Ingredient additionIngredient;

    public DragonSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(id, base, addition, result);
        this.template = template;
        this.baseIngredient = base;
        this.additionIngredient = addition;
    }

    @Override
    public boolean matches(Container input, Level level) {
        return input.getContainerSize() >= 3 && template.test(input.getItem(0))
                && baseIngredient.test(input.getItem(1)) && additionIngredient.test(input.getItem(2));
    }

    @Override
    public ItemStack assemble(Container input) {
        ItemStack result = getResultItem().copy();
        if (input.getItem(1).hasTag()) {
            result.setTag(input.getItem(1).getTag().copy());
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, template, baseIngredient, additionIngredient);
    }

    public Ingredient getTemplate() {
        return template;
    }

    public Ingredient getBase() {
        return baseIngredient;
    }

    public Ingredient getAddition() {
        return additionIngredient;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRAGON_SMITHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DRAGON_SMITHING.get();
    }

    @Override
    public boolean isIncomplete() {
        return template.isEmpty() || super.isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<DragonSmithingRecipe> {
        @Override
        public DragonSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new DragonSmithingRecipe(id, Ingredient.fromJson(json.get("template")),
                    Ingredient.fromJson(json.get("base")), Ingredient.fromJson(json.get("addition")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")));
        }

        @Override
        public DragonSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new DragonSmithingRecipe(id, Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer),
                    Ingredient.fromNetwork(buffer), buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DragonSmithingRecipe recipe) {
            recipe.template.toNetwork(buffer);
            recipe.baseIngredient.toNetwork(buffer);
            recipe.additionIngredient.toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem());
        }
    }
}
