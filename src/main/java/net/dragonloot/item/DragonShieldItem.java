package net.dragonloot.item;

import net.dragonloot.client.render.DragonShieldRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class DragonShieldItem extends ShieldItem {
    public static final int FALLBACK_DISABLE_COOLDOWN_TICKS = 40;

    public DragonShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DragonShieldRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new DragonShieldRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
