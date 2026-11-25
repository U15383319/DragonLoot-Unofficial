package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;

public class DragonAxeItem extends AxeItem {
    public DragonAxeItem(IItemTier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity defender, LivingEntity attacker) {
        boolean canDisable = super.canDisableShield(stack, shield, defender, attacker);
        if (canDisable && defender instanceof PlayerEntity) {
            DragonLoot.trackDragonAxeShieldDisable((PlayerEntity) defender, shield);
        }
        return canDisable;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
