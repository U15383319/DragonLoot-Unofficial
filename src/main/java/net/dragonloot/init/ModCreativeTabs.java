package net.dragonloot.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static final CreativeModeTab DRAGONLOOT = new CreativeModeTab("dragonloot.dragonloot") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.DRAGON_SCALE.get());
        }
    };

    private ModCreativeTabs() {
    }
}
