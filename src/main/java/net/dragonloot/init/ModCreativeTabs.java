package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonLoot.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DRAGONLOOT = CREATIVE_MODE_TABS.register("dragonloot",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dragonloot.dragonloot"))
                    .icon(() -> new ItemStack(ModItems.DRAGON_SCALE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DRAGON_ANVIL.get());
                        output.accept(ModItems.DRAGON_SCALE.get());
                        output.accept(ModItems.DRAGON_HELMET.get());
                        output.accept(ModItems.DRAGON_CHESTPLATE.get());
                        output.accept(ModItems.DRAGON_LEGGINGS.get());
                        output.accept(ModItems.DRAGON_BOOTS.get());
                        output.accept(ModItems.UPGRADED_DRAGON_CHESTPLATE.get());
                        output.accept(ModItems.DRAGON_PICKAXE.get());
                        output.accept(ModItems.DRAGON_AXE.get());
                        output.accept(ModItems.DRAGON_SHOVEL.get());
                        output.accept(ModItems.DRAGON_HOE.get());
                        output.accept(ModItems.DRAGON_SWORD.get());
                        output.accept(ModItems.DRAGON_BOW.get());
                        output.accept(ModItems.DRAGON_CROSSBOW.get());
                        output.accept(ModItems.DRAGON_TRIDENT.get());
                        output.accept(ModItems.DRAGON_HORSE_ARMOR.get());
                    }).build());

    private ModCreativeTabs() {
    }
}
