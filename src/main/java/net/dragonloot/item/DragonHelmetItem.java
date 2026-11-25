package net.dragonloot.item;

import java.util.function.Consumer;
import net.dragonloot.client.render.DragonHelmetModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class DragonHelmetItem extends DragonArmorItem {
    public DragonHelmetItem(Properties properties) {
        super(DragonArmorMaterial.INSTANCE, EquipmentSlot.HEAD, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "dragonloot:textures/entity/dragon_helmet_3d.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DragonHelmetModel model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (model == null) {
                    model = new DragonHelmetModel();
                }
                return model;
            }
        });
    }
}
