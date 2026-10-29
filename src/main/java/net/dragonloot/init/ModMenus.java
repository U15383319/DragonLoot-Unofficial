package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<ContainerType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, DragonLoot.MOD_ID);
    public static final RegistryObject<ContainerType<DragonSmithingMenu>> DRAGON_SMITHING =
            MENUS.register("dragon_smithing", () -> new ContainerType<>(DragonSmithingMenu::new));

    private ModMenus() {
    }
}
