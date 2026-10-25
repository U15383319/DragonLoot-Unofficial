package net.dragonloot.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class DragonCrossbowItem extends CrossbowItem {
    public DragonCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (CrossbowItem.isCharged(stack)) {
            float power = CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
            CrossbowItem.performShooting(level, player, hand, stack, power, 1.0F);
            CrossbowItem.setCharged(stack, false);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
