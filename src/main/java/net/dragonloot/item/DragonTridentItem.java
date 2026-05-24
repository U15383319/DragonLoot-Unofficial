package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.client.render.DragonTridentItemRenderer;
import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class DragonTridentItem extends TridentItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public DragonTridentItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier",
                8.0D + DragonLootConfig.itemBaseDamage() / 5.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier",
                -2.9D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DragonTridentItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new DragonTridentItemRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return;
        }

        int usedTicks = getUseDuration(stack) - remainingUseTicks;
        if (usedTicks < 10) {
            return;
        }

        int riptide = EnchantmentHelper.getRiptide(stack);
        if (riptide > 0 && !player.isInWaterOrRain() && !player.isInLava()) {
            return;
        }

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(user.getUsedItemHand()));
            if (riptide == 0) {
                DragonTridentEntity trident = new DragonTridentEntity(level, player, stack);
                trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                if (player.getAbilities().instabuild) {
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.addFreshEntity(trident);
                level.playSound(null, trident, SoundEvents.TRIDENT_THROW, player.getSoundSource(), 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (riptide > 0) {
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            float x = -net.minecraft.util.Mth.sin(yaw * ((float) Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float) Math.PI / 180F));
            float y = -net.minecraft.util.Mth.sin(pitch * ((float) Math.PI / 180F));
            float z = net.minecraft.util.Mth.cos(yaw * ((float) Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float) Math.PI / 180F));
            float length = net.minecraft.util.Mth.sqrt(x * x + y * y + z * z);
            float strength = 3.0F * ((1.0F + riptide) / 4.0F);
            player.push(x * strength / length, y * strength / length, z * strength / length);
            player.startAutoSpinAttack(20);
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999D, 0.0D));
            }
            level.playSound(null, player, riptide >= 3 ? SoundEvents.TRIDENT_RIPTIDE_3 : riptide == 2 ? SoundEvents.TRIDENT_RIPTIDE_2 : SoundEvents.TRIDENT_RIPTIDE_1,
                    player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        if (EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain() && !player.isInLava()) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(source);
    }
}
