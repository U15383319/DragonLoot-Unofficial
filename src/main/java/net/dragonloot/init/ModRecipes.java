package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.recipe.DragonSmithingRecipe;
import net.minecraft.util.registry.Registry;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DragonLoot.MOD_ID);
    public static final RegistryObject<IRecipeSerializer<DragonSmithingRecipe>> DRAGON_SMITHING_SERIALIZER =
            SERIALIZERS.register("smithing_transform", DragonSmithingRecipe.Serializer::new);
    public static final IRecipeType<DragonSmithingRecipe> DRAGON_SMITHING = new IRecipeType<DragonSmithingRecipe>() {
        @Override
        public String toString() {
            return DragonLoot.id("smithing_transform").toString();
        }
    };

    public static void registerType() {
        Registry.register(Registry.RECIPE_TYPE, DragonLoot.id("smithing_transform"), DRAGON_SMITHING);
    }

    private ModRecipes() {
    }
}
