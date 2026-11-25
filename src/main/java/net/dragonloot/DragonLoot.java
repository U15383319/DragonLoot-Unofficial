package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModMenus;
import net.dragonloot.init.ModRecipes;
import net.dragonloot.menu.DragonSmithingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;
import net.dragonloot.item.DragonCrossbowItem;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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

import java.util.Map;
import java.util.WeakHashMap;

@Mod(DragonLoot.MOD_ID)
public class DragonLoot {
    public static final String MOD_ID = "dragonloot";
    private static final Map<Player, PendingShieldDisable> PENDING_DRAGON_AXE_DISABLES = new WeakHashMap<>();
    private final Map<Player, Object> adjustedDragonShieldCooldowns = new WeakHashMap<>();

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
        if (event.getEntity().isShiftKeyDown() || !event.getLevel().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, user) ->
                    new DragonSmithingMenu(id, inventory, ContainerLevelAccess.create(event.getLevel(), event.getPos())),
                    Component.translatable("container.upgrade")));
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
        if (event.getEntity().containerMenu instanceof net.dragonloot.menu.DragonAnvilMenu) {
            event.setBreakChance(0.0F);
        }
    }

    public static void trackDragonAxeShieldDisable(Player defender, ItemStack blockingStack) {
        int bonus = DragonLootConfig.dragonAxeShieldDisableBonusTicks();
        if (bonus == 0 || defender.level.isClientSide || blockingStack.isEmpty()) {
            return;
        }

        // Forge evaluates canDisableShield before it applies the shield cooldown.
        Item shield = blockingStack.getItem();
        Object previous = getCooldownEntries(defender.getCooldowns()).get(shield);
        PENDING_DRAGON_AXE_DISABLES.put(defender, new PendingShieldDisable(shield, previous, bonus));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide) {
            return;
        }

        PendingShieldDisable pending = PENDING_DRAGON_AXE_DISABLES.remove(player);
        ItemCooldowns cooldowns = player.getCooldowns();
        Item dragonShield = ModItems.DRAGON_SHIELD.get();
        if (pending == null && !cooldowns.isOnCooldown(dragonShield)) {
            adjustedDragonShieldCooldowns.remove(player);
            return;
        }

        Map<Item, ?> entries = getCooldownEntries(cooldowns);
        int tickCount = ObfuscationReflectionHelper.getPrivateValue(ItemCooldowns.class, cooldowns, "f_41516_");
        if (pending != null && pending.shield != dragonShield) {
            Object active = entries.get(pending.shield);
            if (pending.hasNewCooldown(active)) {
                int remaining = getCooldownInstanceField(active, "f_41534_") - tickCount;
                cooldowns.addCooldown(pending.shield, remaining + pending.bonus);
            }
        }

        Object active = entries.get(dragonShield);
        if (active == null) {
            adjustedDragonShieldCooldowns.remove(player);
            return;
        }
        if (active == adjustedDragonShieldCooldowns.get(player)) {
            return;
        }

        int bonus = pending != null && pending.shield == dragonShield && pending.hasNewCooldown(active) ? pending.bonus : 0;
        int startTime = getCooldownInstanceField(active, "f_41533_");
        int endTime = getCooldownInstanceField(active, "f_41534_");
        int remaining = endTime - tickCount;
        int fullDuration = endTime - startTime + bonus;
        int reduced = Math.max(1, Mth.ceil(fullDuration * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
        int duration = Math.min(remaining + bonus, reduced);
        if (duration != remaining) {
            cooldowns.addCooldown(dragonShield, duration);
        }
        adjustedDragonShieldCooldowns.put(player, entries.get(dragonShield));
    }

    private static Map<Item, ?> getCooldownEntries(ItemCooldowns cooldowns) {
        return ObfuscationReflectionHelper.getPrivateValue(ItemCooldowns.class, cooldowns, "f_41515_");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int getCooldownInstanceField(Object cooldown, String fieldName) {
        return (Integer) ObfuscationReflectionHelper.getPrivateValue((Class) cooldown.getClass(), cooldown, fieldName);
    }

    private static final class PendingShieldDisable {
        private final Item shield;
        private final Object previous;
        private final int bonus;

        private PendingShieldDisable(Item shield, Object previous, int bonus) {
            this.shield = shield;
            this.previous = previous;
            this.bonus = bonus;
        }

        private boolean hasNewCooldown(Object active) {
            return active != null && active != previous;
        }
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
