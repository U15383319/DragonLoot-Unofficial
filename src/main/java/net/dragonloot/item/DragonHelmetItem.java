package net.dragonloot.item;

import net.dragonloot.client.render.DragonHelmetModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class DragonHelmetItem extends DragonArmorItem {
    public DragonHelmetItem(Properties properties) {
        super(DragonArmorMaterial.INSTANCE, EquipmentSlotType.HEAD, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "dragonloot:textures/entity/dragon_helmet_3d.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlotType slot, A original) {
        return (A) ClientModel.HELMET;
    }

    private static final class ClientModel {
        private static final DragonHelmetModel HELMET = new DragonHelmetModel();
    }
}
