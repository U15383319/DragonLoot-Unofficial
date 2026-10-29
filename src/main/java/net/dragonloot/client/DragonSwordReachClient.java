package net.dragonloot.client;

import net.dragonloot.DragonLoot;
import net.dragonloot.item.DragonSwordReach;
import net.dragonloot.network.DragonLootNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonLoot.MOD_ID, value = Dist.CLIENT)
public final class DragonSwordReachClient {
    private DragonSwordReachClient() {
    }

    @SubscribeEvent
    public static void updateTarget(RenderWorldLastEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getCameraEntity() != client.player) {
            return;
        }
        EntityRayTraceResult hit = DragonSwordReach.pick(client.player, event.getPartialTicks());
        if (hit != null) {
            client.hitResult = hit;
            client.crosshairPickEntity = hit.getEntity();
        }
    }

    @SubscribeEvent
    public static void attackExtendedTarget(InputEvent.ClickInputEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.isUseItem() && client.player != null) {
            client.gameRenderer.pick(1.0F);
            return;
        }
        if (!event.isAttack() || client.player == null || client.gameMode == null || client.gameMode.isDestroying()) {
            return;
        }
        EntityRayTraceResult hit = DragonSwordReach.pick(client.player, 1.0F);
        double base = DragonSwordReach.baseRange(client.player);
        if (hit != null && client.player.getEyePosition(1.0F).distanceToSqr(hit.getLocation()) > base * base) {
            DragonLootNetwork.attack(hit.getEntity());
            client.player.swing(Hand.MAIN_HAND);
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }
}
