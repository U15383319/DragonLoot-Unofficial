package net.dragonloot.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class ModCreativeTabs {
    public static final ItemGroup DRAGONLOOT = new ItemGroup("dragonloot.dragonloot") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.DRAGON_SCALE.get());
        }
    };

    private ModCreativeTabs() {
    }
}
