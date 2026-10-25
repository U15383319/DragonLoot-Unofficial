package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DragonLoot.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, DragonLoot.MOD_ID);
    public static final RegistryObject<RecipeSerializer<DragonSmithingRecipe>> DRAGON_SMITHING_SERIALIZER =
            SERIALIZERS.register("smithing_transform", DragonSmithingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<DragonSmithingRecipe>> DRAGON_SMITHING =
            TYPES.register("smithing_transform", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return DragonLoot.id("smithing_transform").toString();
                }
            });

    private ModRecipes() {
    }
}
