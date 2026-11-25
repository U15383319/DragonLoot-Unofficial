package net.dragonloot;

import net.dragonloot.config.DragonLootConfig;
import net.dragonloot.init.ModBlocks;
import net.dragonloot.init.ModEntities;
import net.dragonloot.init.ModItems;
import net.dragonloot.init.ModAttributes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.CooldownTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Map;
import java.util.WeakHashMap;

@Mod(DragonLoot.MOD_ID)
public class DragonLoot {
    public static final String MOD_ID = "dragonloot";
    private static final Map<PlayerEntity, PendingShieldDisable> PENDING_DRAGON_AXE_DISABLES = new WeakHashMap<>();
    private final Map<PlayerEntity, Object> adjustedDragonShieldCooldowns = new WeakHashMap<>();

    public DragonLoot() {
        DragonLootConfig.loadBeforeRegistration();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DragonLootConfig.SPEC, DragonLootConfig.FILE_NAME);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModAttributes.ATTRIBUTES.register(modBus);
        net.dragonloot.network.DragonLootNetwork.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @SubscribeEvent
    public void onMissingItems(RegistryEvent.MissingMappings<Item> event) {
        for (var mapping : event.getMappings(MOD_ID)) {
            if (mapping.key.equals(id("dragon_upgrade_smithing_template"))) {
                mapping.ignore();
            }
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
    public void onDragonAnvilRepair(AnvilRepairEvent event) {
        if (event.getPlayer().containerMenu instanceof net.dragonloot.menu.DragonAnvilMenu) {
            event.setBreakChance(0.0F);
        }
    }

    public static void trackDragonAxeShieldDisable(PlayerEntity defender, ItemStack blockingStack) {
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
        PlayerEntity player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide) {
            return;
        }

        PendingShieldDisable pending = PENDING_DRAGON_AXE_DISABLES.remove(player);
        CooldownTracker cooldowns = player.getCooldowns();
        Item dragonShield = ModItems.DRAGON_SHIELD.get();
        if (pending == null && !cooldowns.isOnCooldown(dragonShield)) {
            adjustedDragonShieldCooldowns.remove(player);
            return;
        }

        Map<Item, ?> entries = getCooldownEntries(cooldowns);
        int tickCount = ObfuscationReflectionHelper.getPrivateValue(CooldownTracker.class, cooldowns, "field_185148_b");
        if (pending != null && pending.shield != dragonShield) {
            Object active = entries.get(pending.shield);
            if (pending.hasNewCooldown(active)) {
                int remaining = getCooldownInstanceField(active, "field_185138_b") - tickCount;
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
        int startTime = getCooldownInstanceField(active, "field_185137_a");
        int endTime = getCooldownInstanceField(active, "field_185138_b");
        int remaining = endTime - tickCount;
        int fullDuration = endTime - startTime + bonus;
        int reduced = Math.max(1, MathHelper.ceil(fullDuration * DragonLootConfig.dragonShieldDisableCooldownMultiplier()));
        int duration = Math.min(remaining + bonus, reduced);
        if (duration != remaining) {
            cooldowns.addCooldown(dragonShield, duration);
        }
        adjustedDragonShieldCooldowns.put(player, entries.get(dragonShield));
    }

    private static Map<Item, ?> getCooldownEntries(CooldownTracker cooldowns) {
        return ObfuscationReflectionHelper.getPrivateValue(CooldownTracker.class, cooldowns, "field_185147_a");
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
}
