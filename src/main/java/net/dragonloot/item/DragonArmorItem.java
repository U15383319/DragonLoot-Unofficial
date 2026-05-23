package net.dragonloot.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DragonArmorItem extends ArmorItem {
    private static final UUID[] ARMOR_UUIDS = new UUID[]{
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };
    private final Multimap<Attribute, AttributeModifier> dragonModifiers;
    private final boolean winged;

    public DragonArmorItem(ArmorMaterial material, Type type, Properties properties) {
        this(material, type, properties, false);
    }

    public DragonArmorItem(ArmorMaterial material, Type type, Properties properties, boolean winged) {
        super(material, type, properties);
        this.winged = winged;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_UUIDS[type.getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier",
                material.getDefenseForType(type), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness",
                material.getToughness(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid,
                "Armor knockback resistance", material.getKnockbackResistance() / 10.0D, AttributeModifier.Operation.ADDITION));
        this.dragonModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == getEquipmentSlot() ? dragonModifiers : super.getDefaultAttributeModifiers(slot);
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
            entity.setItemSlot(EquipmentSlot.CHEST, brokenElytra);
            entity.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
        }
        return winged;
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(source);
    }
}
