package net.dragonloot;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID)
public final class DragonLootServerSmokeTest {
    private DragonLootServerSmokeTest() {
    }

    @SubscribeEvent
    public static void onStarted(FMLServerStartedEvent event) {
        if (Boolean.getBoolean("dragonloot.verifyServer")) {
            DragonLootGameTests.runAll(event.getServer().overworld());
            event.getServer().halt(false);
        }
    }
}
