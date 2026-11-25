package net.dragonloot.item;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DragonArmorItem extends ArmorItem {
    private final boolean winged;

    public DragonArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        this(material, type, properties, false);
    }

    public DragonArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, boolean winged) {
        super(material, type, properties.durability(DragonArmorMaterial.durability(type)));
        this.winged = winged;
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return winged;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (winged && flightTicks % 20 == 0 && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            recoverBrokenElytra(entity);
            entity.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
        }
        return winged;
    }

    public static void recoverBrokenElytra(LivingEntity entity) {
        ItemStack brokenElytra = new ItemStack(Items.ELYTRA);
        brokenElytra.setDamageValue(brokenElytra.getMaxDamage());
        entity.setItemSlot(EquipmentSlot.CHEST, brokenElytra);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
