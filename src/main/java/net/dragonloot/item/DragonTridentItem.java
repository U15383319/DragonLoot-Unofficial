package net.dragonloot.item;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DragonTridentItem extends TridentItem {
    public DragonTridentItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, createToolProperties())
                .attributes(createAttributes().withModifierAdded(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)));
    }

    public static float attackDamage() {
        return BASE_DAMAGE + DragonLootConfig.itemBaseDamage() / 5.0F;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player) || getUseDuration(stack, user) - remainingUseTicks < THROW_THRESHOLD_TIME
                || stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return;
        }

        float spinStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (spinStrength > 0.0F && !canSpin(player)) {
            return;
        }
        var sound = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                .orElse(SoundEvents.TRIDENT_THROW).value();

        if (!level.isClientSide()) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(user.getUsedItemHand()));
            if (spinStrength == 0.0F) {
                DragonTridentEntity trident = new DragonTridentEntity(level, player, stack);
                trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SHOOT_POWER, 1.0F);
                if (player.hasInfiniteMaterials()) {
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                if (level.addFreshEntity(trident)) {
                    level.playSound(null, trident, sound, player.getSoundSource(), 1.0F, 1.0F);
                    if (!player.hasInfiniteMaterials()) {
                        player.getInventory().removeItem(stack);
                    }
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (spinStrength > 0.0F) {
            Vec3 velocity = player.getLookAngle().scale(spinStrength);
            player.push(velocity.x, velocity.y, velocity.z);
            player.startAutoSpinAttack(20, attackDamage(), stack);
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
            }
            level.playSound(null, player, sound, player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1
                || (EnchantmentHelper.getTridentSpinAttackStrength(stack, player) > 0.0F && !canSpin(player))) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    private static boolean canSpin(Player player) {
        return player.isInWaterOrRain() || player.isInLava();
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        DragonTridentEntity trident = new DragonTridentEntity(level, position, stack);
        trident.pickup = AbstractArrow.Pickup.ALLOWED;
        return trident;
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
