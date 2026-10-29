package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.client.render.DragonTridentItemRenderer;
import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.vector.Vector3d;

public class DragonTridentItem extends TridentItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public DragonTridentItem(Properties properties) {
        super(properties.setISTER(() -> DragonTridentItemRenderer::new));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier",
                8.0D + DragonLootConfig.itemBaseDamage() / 5.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier",
                -2.9D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public void releaseUsing(ItemStack stack, World level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) user;

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
                trident.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F, 1.0F);
                if (player.abilities.instabuild) {
                    trident.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                }
                level.addFreshEntity(trident);
                level.playSound(null, trident, SoundEvents.TRIDENT_THROW, player.getSoundSource(), 1.0F, 1.0F);
                if (!player.abilities.instabuild) {
                    player.inventory.removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (riptide > 0) {
            float yaw = player.yRot;
            float pitch = player.xRot;
            float x = -net.minecraft.util.math.MathHelper.sin(yaw * ((float) Math.PI / 180F)) * net.minecraft.util.math.MathHelper.cos(pitch * ((float) Math.PI / 180F));
            float y = -net.minecraft.util.math.MathHelper.sin(pitch * ((float) Math.PI / 180F));
            float z = net.minecraft.util.math.MathHelper.cos(yaw * ((float) Math.PI / 180F)) * net.minecraft.util.math.MathHelper.cos(pitch * ((float) Math.PI / 180F));
            float length = net.minecraft.util.math.MathHelper.sqrt(x * x + y * y + z * z);
            float strength = 3.0F * ((1.0F + riptide) / 4.0F);
            player.push(x * strength / length, y * strength / length, z * strength / length);
            player.startAutoSpinAttack(20);
            if (player.isOnGround()) {
                player.move(MoverType.SELF, new Vector3d(0.0D, 1.1999999D, 0.0D));
            }
            level.playSound(null, player, riptide >= 3 ? SoundEvents.TRIDENT_RIPTIDE_3 : riptide == 2 ? SoundEvents.TRIDENT_RIPTIDE_2 : SoundEvents.TRIDENT_RIPTIDE_1,
                    player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return ActionResult.fail(stack);
        }
        if (EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain() && !player.isInLava()) {
            return ActionResult.fail(stack);
        }
        player.startUsingItem(hand);
        return ActionResult.consume(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
