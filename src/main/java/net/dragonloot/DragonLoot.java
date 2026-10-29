package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.init.ModAttributes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.block.Blocks;
import net.dragonloot.item.DragonCrossbowItem;
import net.dragonloot.item.DragonShieldItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.util.CooldownTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.loot.ConstantRange;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
        ModAttributes.ATTRIBUTES.register(modBus);
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(ModRecipes::registerType));
        net.dragonloot.network.DragonLootNetwork.register();

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
        event.setCancellationResult(ActionResultType.sidedSuccess(event.getWorld().isClientSide));
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            NetworkHooks.openGui(player, new SimpleNamedContainerProvider((id, inventory, user) ->
                    new DragonSmithingMenu(id, inventory, IWorldPosCallable.create(event.getWorld(), event.getPos())),
                    new TranslationTextComponent("container.upgrade")));
        }
    }

    @SubscribeEvent
    public void onDragonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EnderDragonEntity) || event.getEntity().level.isClientSide()) {
            return;
        }
        EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

        int nearbyPlayers = dragon.level.getEntitiesOfClass(
                net.minecraft.entity.player.PlayerEntity.class,
                new AxisAlignedBB(dragon.blockPosition()).inflate(128.0D),
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
        if (!(event.getEntity() instanceof AbstractArrowEntity)) {
            return;
        }
        AbstractArrowEntity arrow = (AbstractArrowEntity) event.getEntity();
        if (!(arrow.getOwner() instanceof net.minecraft.entity.LivingEntity)) {
            return;
        }
        net.minecraft.entity.LivingEntity owner = (net.minecraft.entity.LivingEntity) arrow.getOwner();

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
    private int getReducedDragonShieldCooldownTicks(CooldownTracker itemCooldowns) {
        try {
            Map<Item, ?> cooldowns = ObfuscationReflectionHelper.getPrivateValue(CooldownTracker.class, itemCooldowns, "field_185147_a");
            Integer tickCount = ObfuscationReflectionHelper.getPrivateValue(CooldownTracker.class, itemCooldowns, "field_185148_b");
            Object cooldown = cooldowns == null ? null : cooldowns.get(ModItems.DRAGON_SHIELD.get());
            if (cooldown == null || tickCount == null) {
                return DragonShieldItem.FALLBACK_DISABLE_COOLDOWN_TICKS;
            }

            int startTime = getCooldownInstanceField(cooldown, "field_185137_a");
            int endTime = getCooldownInstanceField(cooldown, "field_185138_b");
            int originalDuration = Math.max(1, endTime - startTime);
            int remainingDuration = Math.max(1, endTime - tickCount);
            int reducedDuration = Math.max(1, MathHelper.ceil(originalDuration * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
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
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(ModItems.DRAGON_UPGRADE_SMITHING_TEMPLATE.get()))
                    .when(RandomChance.randomChance(0.25F))
                    .build());
        }
    }
}
