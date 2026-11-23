package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.menu.DragonAnvilMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, DragonLoot.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DragonAnvilMenu>> DRAGON_ANVIL =
            MENUS.register("dragon_anvil", () -> new MenuType<>(DragonAnvilMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }
}
