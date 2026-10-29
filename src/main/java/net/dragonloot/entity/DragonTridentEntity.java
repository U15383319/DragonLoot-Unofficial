package net.dragonloot.entity;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import javax.annotation.Nullable;

public class DragonTridentEntity extends AbstractArrowEntity {
    private static final DataParameter<Byte> ID_LOYALTY = EntityDataManager.defineId(DragonTridentEntity.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> ID_FOIL = EntityDataManager.defineId(DragonTridentEntity.class, DataSerializers.BOOLEAN);

    private ItemStack tridentStack = new ItemStack(ModItems.DRAGON_TRIDENT.get());
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public DragonTridentEntity(EntityType<? extends DragonTridentEntity> type, World level) {
        super(type, level);
    }

    public DragonTridentEntity(World level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.DRAGON_TRIDENT.get(), owner, level);
        this.tridentStack = stack.copy();
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
        this.entityData.set(ID_FOIL, stack.hasFoil());
    }

    @Override
    public net.minecraft.network.IPacket<?> getAddEntityPacket() {
        return net.minecraftforge.fml.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
        this.entityData.define(ID_FOIL, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if ((this.dealtDamage || this.isNoPhysics()) && owner != null && loyalty > 0) {
            if (!isAcceptableReturnOwner()) {
                if (!this.level.isClientSide && this.pickup == PickupStatus.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.remove();
            } else {
                this.setNoPhysics(true);
                Vector3d returnVector = owner.getEyePosition(1.0F).subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + returnVector.y * 0.015D * loyalty, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(returnVector.normalize().scale(0.05D * loyalty)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.clientSideReturnTridentTickCount++;
            }
        }

        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity owner = this.getOwner();
        return owner != null && owner.isAlive() && (!(owner instanceof ServerPlayerEntity) || !owner.isSpectator());
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.tridentStack.copy();
    }

    @Override
    @Nullable
    protected EntityRayTraceResult findHitEntity(Vector3d startVec, Vector3d endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        Entity target = result.getEntity();
        float damage = (float) (8.0D + DragonLootConfig.itemBaseDamage() / 5.0D);
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            damage += EnchantmentHelper.getDamageBonus(this.tridentStack, livingTarget.getMobType());
        }

        Entity owner = this.getOwner();
        var damageSource = DamageSource.trident(this, owner == null ? this : owner);
        this.dealtDamage = true;
        SoundEvent sound = SoundEvents.TRIDENT_HIT;
        if (target.hurt(damageSource, damage)) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (owner instanceof LivingEntity) {
                    LivingEntity livingOwner = (LivingEntity) owner;
                    EnchantmentHelper.doPostHurtEffects(livingTarget, owner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
                }
                this.doPostHurtEffects(livingTarget);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float volume = 1.0F;
        if (this.level instanceof ServerWorld && this.level.isThundering() && EnchantmentHelper.hasChanneling(this.tridentStack)) {
            ServerWorld serverLevel = (ServerWorld) this.level;
            var pos = target.blockPosition();
            if (serverLevel.canSeeSky(pos)) {
                LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightning != null) {
                    lightning.moveTo(Vector3d.atBottomCenterOf(pos));
                    lightning.setCause(owner instanceof ServerPlayerEntity ? (ServerPlayerEntity) owner : null);
                    serverLevel.addFreshEntity(lightning);
                    sound = SoundEvents.TRIDENT_THUNDER;
                    volume = 5.0F;
                }
            }
        }
        this.playSound(sound, volume, 1.0F);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(PlayerEntity player) {
        Entity owner = this.getOwner();
        if (owner == null || owner.getUUID().equals(player.getUUID())) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Dragon_Trident", 10)) {
            this.tridentStack = ItemStack.of(tag.getCompound("Dragon_Trident"));
        }
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.tridentStack));
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Dragon_Trident", this.tridentStack.save(new CompoundNBT()));
        tag.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    protected void tickDespawn() {
        if (this.pickup != PickupStatus.ALLOWED || this.entityData.get(ID_LOYALTY) <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
