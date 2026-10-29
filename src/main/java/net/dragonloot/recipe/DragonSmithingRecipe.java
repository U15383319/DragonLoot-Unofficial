package net.dragonloot.recipe;

import com.google.gson.JsonObject;
import net.dragonloot.init.ModRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.JSONUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class DragonSmithingRecipe extends SmithingRecipe {
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
    public boolean matches(IInventory input, World level) {
        return input.getContainerSize() >= 3 && template.test(input.getItem(0))
                && baseIngredient.test(input.getItem(1)) && additionIngredient.test(input.getItem(2));
    }

    @Override
    public ItemStack assemble(IInventory input) {
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
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRAGON_SMITHING_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return ModRecipes.DRAGON_SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<DragonSmithingRecipe> {
        @Override
        public DragonSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new DragonSmithingRecipe(id, Ingredient.fromJson(json.get("template")),
                    Ingredient.fromJson(json.get("base")), Ingredient.fromJson(json.get("addition")),
                    ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "result")));
        }

        @Override
        public DragonSmithingRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            return new DragonSmithingRecipe(id, Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer),
                    Ingredient.fromNetwork(buffer), buffer.readItem());
        }

        @Override
        public void toNetwork(PacketBuffer buffer, DragonSmithingRecipe recipe) {
            recipe.template.toNetwork(buffer);
            recipe.baseIngredient.toNetwork(buffer);
            recipe.additionIngredient.toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem());
        }
    }
}
