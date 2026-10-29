package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, DragonLoot.MOD_ID);
    // Forge 1.16.5 only provides block reach, so attacks use a separate equipment attribute.
    public static final RegistryObject<Attribute> ATTACK_RANGE_BONUS = ATTRIBUTES.register("attack_range_bonus",
            () -> new RangedAttribute("attribute.name.dragonloot.attack_range_bonus", 0.0D, 0.0D, 1024.0D).setSyncable(true));

    private ModAttributes() {
    }

    @SubscribeEvent
    public static void addPlayerAttribute(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ATTACK_RANGE_BONUS.get());
    }
}
