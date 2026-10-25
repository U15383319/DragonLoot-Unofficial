package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, DragonLoot.MOD_ID);
    public static final RegistryObject<MenuType<DragonSmithingMenu>> DRAGON_SMITHING =
            MENUS.register("dragon_smithing", () -> new MenuType<>(DragonSmithingMenu::new));

    private ModMenus() {
    }
}
