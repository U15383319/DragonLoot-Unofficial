package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DragonArmorItem extends ArmorItem {
    private static final UUID[] ARMOR_UUIDS = new UUID[]{
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };
    private final Multimap<Attribute, AttributeModifier> dragonModifiers;
    private final boolean winged;

    public DragonArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties) {
        this(material, slot, properties, false);
    }

    public DragonArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties, boolean winged) {
        super(material, slot, properties);
        this.winged = winged;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_UUIDS[slot.getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier",
                material.getDefenseForSlot(slot), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness",
                material.getToughness(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid,
                "Armor knockback resistance", material.getKnockbackResistance() / 10.0D, AttributeModifier.Operation.ADDITION));
        this.dragonModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        return slot == getSlot() ? dragonModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return winged;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (winged && flightTicks % 20 == 0 && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            ItemStack brokenElytra = new ItemStack(Items.ELYTRA);
            brokenElytra.setDamageValue(Items.ELYTRA.getMaxDamage(brokenElytra));
            entity.setItemSlot(EquipmentSlotType.CHEST, brokenElytra);
            entity.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
        }
        return winged;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.isExplosion() && super.canBeHurtBy(source);
    }
}
