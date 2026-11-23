package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModCreativeTabs;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.item.DragonArmorMaterial;
import net.dragonloot.menu.DragonAnvilMenu;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import java.util.Map;
import java.util.WeakHashMap;

@Mod(DragonLoot.MOD_ID)
public class DragonLoot {
    public static final String MOD_ID = "dragonloot";
    private final Map<Player, ItemCooldowns.CooldownInstance> adjustedDragonShieldCooldowns = new WeakHashMap<>();

        public DragonLoot(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.STARTUP, DragonLootConfig.SPEC, "dragonloot-common.toml");
        DragonArmorMaterial.MATERIALS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModMenus.MENUS.register(modBus);

        NeoForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @SubscribeEvent
    public void onDragonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level().isClientSide()) {
            return;
        }

        int nearbyPlayers = dragon.level().getEntitiesOfClass(
                Player.class,
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
    public void onDragonAnvilRepair(AnvilRepairEvent event) {
        if (event.getEntity().containerMenu instanceof DragonAnvilMenu) {
            event.setBreakChance(0.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        ItemCooldowns cooldowns = player.getCooldowns();
        var shield = ModItems.DRAGON_SHIELD.get();
        var active = cooldowns.cooldowns.get(shield);
        if (active == null) {
            adjustedDragonShieldCooldowns.remove(player);
            return;
        }
        if (active == adjustedDragonShieldCooldowns.get(player)) {
            return;
        }

        int original = active.endTime - active.startTime;
        int remaining = active.endTime - cooldowns.tickCount;
        int duration = Math.max(1, Mth.ceil(original * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
        if (duration < remaining) {
            cooldowns.addCooldown(shield, duration);
        }
        adjustedDragonShieldCooldowns.put(player, cooldowns.cooldowns.get(shield));
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (ResourceLocation.fromNamespaceAndPath("minecraft", "chests/end_city_treasure").equals(event.getName())) {
            event.getTable().addPool(LootPool.lootPool()
                    .name("dragonloot_dragon_upgrade_template")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.25F))
                    .build());
        }
    }
}
