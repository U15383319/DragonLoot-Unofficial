package net.dragonloot.entity;

import com.mojang.serialization.Dynamic;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.item.DragonTridentItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DragonTridentEntity extends ThrownTrident {
    private static final int FORGE_1_20_1_DATA_VERSION = 3465;

    public DragonTridentEntity(EntityType<? extends DragonTridentEntity> type, Level level) {
        super(type, level);
    }

    public DragonTridentEntity(Level level, LivingEntity owner, ItemStack stack) {
        this(ModEntities.DRAGON_TRIDENT.get(), level);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        setOwner(owner);
        initializeItem(stack);
    }

    public DragonTridentEntity(Level level, Position position, ItemStack stack) {
        this(ModEntities.DRAGON_TRIDENT.get(), level);
        setPos(position.x(), position.y(), position.z());
        initializeItem(stack);
    }

    private void initializeItem(ItemStack stack) {
        setPickupItemStack(stack.copyWithCount(1));
        entityData.set(ID_LOYALTY, getLoyaltyFromItem(stack));
        entityData.set(ID_FOIL, stack.hasFoil());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (!tag.contains("item", Tag.TAG_COMPOUND) && tag.contains("Dragon_Trident", Tag.TAG_COMPOUND)) {
            // Earlier Forge versions saved the projectile stack under a custom NBT key.
            var oldStack = new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("Dragon_Trident"));
            var updated = DataFixers.getDataFixer().update(References.ITEM_STACK, oldStack,
                    FORGE_1_20_1_DATA_VERSION, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            if (updated.getValue() instanceof CompoundTag item) {
                tag = tag.copy();
                tag.put("item", item);
            }
        }
        super.readAdditionalSaveData(tag);
        entityData.set(ID_FOIL, getWeaponItem().hasFoil());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DRAGON_TRIDENT.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        var source = damageSources().trident(this, owner == null ? this : owner);
        float damage = DragonTridentItem.attackDamage();
        if (level() instanceof ServerLevel level) {
            damage = EnchantmentHelper.modifyDamage(level, getWeaponItem(), target, source, damage);
        }

        dealtDamage = true;
        if (target.hurt(source, damage)) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (level() instanceof ServerLevel level) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, getWeaponItem());
            }
            if (target instanceof LivingEntity living) {
                doKnockback(living, source);
                doPostHurtEffects(living);
            }
        }

        setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }
}
