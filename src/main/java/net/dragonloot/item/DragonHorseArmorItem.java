package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class DragonHorseArmorItem extends AnimalArmorItem {
    private final ItemAttributeModifiers horseAttributes;

    public DragonHorseArmorItem(Properties properties) {
        super(DragonArmorMaterial.DRAGON, BodyType.EQUESTRIAN, false, properties);
        horseAttributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(DragonLoot.id("horse_armor"),
                        getDefense(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.BODY)
                .build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return horseAttributes;
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.canBeHurtBy(stack, source);
    }
}
