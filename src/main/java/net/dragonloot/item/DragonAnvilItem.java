package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;

public final class DragonAnvilItem extends BlockItem {
    public DragonAnvilItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
