package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

public class DragonAxeItem extends AxeItem {
    public DragonAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity defender, LivingEntity attacker) {
        boolean canDisable = super.canDisableShield(stack, shield, defender, attacker);
        if (canDisable && defender instanceof Player player) {
            DragonLoot.trackDragonAxeShieldDisable(player, shield);
        }
        return canDisable;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(source);
    }
}
