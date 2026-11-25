package net.dragonloot.item;

import net.dragonloot.DragonLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public final class DragonHelmetItem extends DragonArmorItem {
    private static final ResourceLocation TEXTURE = DragonLoot.id("textures/entity/dragon_helmet_3d.png");

    public DragonHelmetItem(Properties properties) {
        super(DragonArmorMaterial.DRAGON, Type.HELMET, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return TEXTURE;
    }
}
