package net.dragonloot.item;

import net.dragonloot.client.render.DragonShieldRenderer;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ShieldItem;

public class DragonShieldItem extends ShieldItem {
    public static final int FALLBACK_DISABLE_COOLDOWN_TICKS = 40;

    public DragonShieldItem(Properties properties) {
        super(properties.setISTER(() -> DragonShieldRenderer::new));
    }

    @Override
    public boolean isShield(net.minecraft.item.ItemStack stack, net.minecraft.entity.LivingEntity entity) {
        return true;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
