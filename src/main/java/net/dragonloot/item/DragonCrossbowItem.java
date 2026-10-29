package net.dragonloot.item;

import net.minecraft.util.DamageSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class DragonCrossbowItem extends CrossbowItem {
    public DragonCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (CrossbowItem.isCharged(stack)) {
            float power = CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
            CrossbowItem.performShooting(level, player, hand, stack, power, 1.0F);
            CrossbowItem.setCharged(stack, false);
            player.awardStat(Stats.ITEM_USED.get(this));
            return ActionResult.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
