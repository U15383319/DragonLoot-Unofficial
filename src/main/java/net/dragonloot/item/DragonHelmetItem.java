package net.dragonloot.item;

import java.util.function.Consumer;
import net.dragonloot.client.render.DragonHelmetModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

public final class DragonHelmetItem extends DragonArmorItem {
    public DragonHelmetItem(Properties properties) {
        super(DragonArmorMaterial.INSTANCE, EquipmentSlot.HEAD, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "dragonloot:textures/entity/dragon_helmet_3d.png";
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            private DragonHelmetModel model;

            @Override
            public HumanoidModel<?> getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (model == null) {
                    model = new DragonHelmetModel();
                }
                return model;
            }
        });
    }
}
