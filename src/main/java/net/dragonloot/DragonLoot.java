package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;
import net.dragonloot.item.DragonCrossbowItem;
import net.dragonloot.item.DragonShieldItem;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(DragonLoot.MOD_ID)
public class DragonLoot {
    public static final String MOD_ID = "dragonloot";
    private final Set<UUID> adjustedDragonShieldCooldowns = new HashSet<>();

    public DragonLoot() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModRecipes.TYPES.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DragonLootConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @SubscribeEvent
    public void onSmithingTableUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getPlayer().isShiftKeyDown() || !event.getWorld().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getWorld().isClientSide));
        if (event.getPlayer() instanceof ServerPlayer player) {
            NetworkHooks.openGui(player, new SimpleMenuProvider((id, inventory, user) ->
                    new DragonSmithingMenu(id, inventory, ContainerLevelAccess.create(event.getWorld(), event.getPos())),
                    new TranslatableComponent("container.upgrade")));
        }
    }

    @SubscribeEvent
    public void onDragonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level.isClientSide()) {
            return;
        }

        int nearbyPlayers = dragon.level.getEntitiesOfClass(
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
            event.getDrops().add(new ItemEntity(dragon.level, dragon.getX(), dragon.getY(), dragon.getZ(),
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
        if (event.getPlayer().containerMenu instanceof net.dragonloot.menu.DragonAnvilMenu) {
            event.setBreakChance(0.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }

        UUID playerId = event.player.getUUID();
        if (!event.player.getCooldowns().isOnCooldown(ModItems.DRAGON_SHIELD.get())) {
            this.adjustedDragonShieldCooldowns.remove(playerId);
            return;
        }

        if (!this.adjustedDragonShieldCooldowns.contains(playerId)
                && event.player.getCooldowns().getCooldownPercent(ModItems.DRAGON_SHIELD.get(), 0.0F) > 0.75F) {
            event.player.getCooldowns().addCooldown(ModItems.DRAGON_SHIELD.get(), this.getReducedDragonShieldCooldownTicks(event.player.getCooldowns()));
            this.adjustedDragonShieldCooldowns.add(playerId);
        }
    }

    @SuppressWarnings("unchecked")
    private int getReducedDragonShieldCooldownTicks(ItemCooldowns itemCooldowns) {
        try {
            Map<Item, ?> cooldowns = ObfuscationReflectionHelper.getPrivateValue(ItemCooldowns.class, itemCooldowns, "f_41515_");
            Integer tickCount = ObfuscationReflectionHelper.getPrivateValue(ItemCooldowns.class, itemCooldowns, "f_41516_");
            Object cooldown = cooldowns == null ? null : cooldowns.get(ModItems.DRAGON_SHIELD.get());
            if (cooldown == null || tickCount == null) {
                return DragonShieldItem.FALLBACK_DISABLE_COOLDOWN_TICKS;
            }

            int startTime = getCooldownInstanceField(cooldown, "f_41533_");
            int endTime = getCooldownInstanceField(cooldown, "f_41534_");
            int originalDuration = Math.max(1, endTime - startTime);
            int remainingDuration = Math.max(1, endTime - tickCount);
            int reducedDuration = Math.max(1, Mth.ceil(originalDuration * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
            return Math.min(remainingDuration, reducedDuration);
        } catch (RuntimeException | LinkageError ignored) {
            return DragonShieldItem.FALLBACK_DISABLE_COOLDOWN_TICKS;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int getCooldownInstanceField(Object cooldown, String fieldName) {
        Integer value = (Integer) ObfuscationReflectionHelper.getPrivateValue((Class) cooldown.getClass(), cooldown, fieldName);
        return value == null ? 0 : value;
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (new ResourceLocation("minecraft", "chests/end_city_treasure").equals(event.getName())) {
            event.getTable().addPool(LootPool.lootPool()
                    .name("dragonloot_dragon_upgrade_template")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.25F))
                    .build());
        }
    }
}
