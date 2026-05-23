package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModCreativeTabs;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.item.DragonCrossbowItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DragonLoot.MOD_ID)
public class DragonLoot {
    public static final String MOD_ID = "dragonloot";

    public DragonLoot() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DragonLootConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @SubscribeEvent
    public void onDragonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level().isClientSide()) {
            return;
        }

        int nearbyPlayers = dragon.level().getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class,
                new AABB(dragon.blockPosition()).inflate(128.0D),
                player -> !player.isSpectator()).size();

        int total = DragonLootConfig.scaleMinimumDropAmount();
        for (int i = 0; i < nearbyPlayers * DragonLootConfig.additionalScalesPerPlayer(); i++) {
            if (dragon.getRandom().nextFloat() <= DragonLootConfig.additionalScaleDropChance()) {
                total++;
            }
        }

        for (int i = 0; i < total; i++) {
            event.getDrops().add(new ItemEntity(dragon.level(), dragon.getX(), dragon.getY(), dragon.getZ(),
                    new ItemStack(ModItems.DRAGON_SCALE.get())));
        }
    }

    @SubscribeEvent
    public void onDragonCrossbowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof net.minecraft.world.entity.LivingEntity owner)) {
            return;
        }

        if (owner.getMainHandItem().getItem() instanceof DragonCrossbowItem
                || owner.getOffhandItem().getItem() instanceof DragonCrossbowItem) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        }
    }

    @SubscribeEvent
    public void onDragonAnvilRepair(AnvilRepairEvent event) {
        if (event.getEntity().containerMenu instanceof net.dragonloot.menu.DragonAnvilMenu) {
            event.setBreakChance(0.0F);
        }
    }
}
