package net.dragonloot.init;

import net.dragonloot.DragonLoot;
import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, DragonLoot.MOD_ID);

    public static final RegistryObject<EntityType<DragonTridentEntity>> DRAGON_TRIDENT = ENTITY_TYPES.register("dragon_trident",
            () -> EntityType.Builder.<DragonTridentEntity>of(DragonTridentEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(DragonLoot.id("dragon_trident").toString()));

    private ModEntities() {
    }
}
