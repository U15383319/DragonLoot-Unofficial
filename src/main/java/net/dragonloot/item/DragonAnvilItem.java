package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public final class DragonAnvilItem extends BlockItem {
    public DragonAnvilItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
