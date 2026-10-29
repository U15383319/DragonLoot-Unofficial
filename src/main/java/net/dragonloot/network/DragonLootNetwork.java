package net.dragonloot.network;

import net.dragonloot.DragonLoot;
import net.dragonloot.item.DragonSwordReach;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class DragonLootNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            DragonLoot.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private DragonLootNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ReachAttack.class, (message, buffer) -> buffer.writeVarInt(message.targetId),
                buffer -> new ReachAttack(buffer.readVarInt()), DragonLootNetwork::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void attack(Entity target) {
        CHANNEL.sendToServer(new ReachAttack(target.getId()));
    }

    private static void handle(ReachAttack message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }
            EntityRayTraceResult hit = DragonSwordReach.pick(player, 1.0F);
            if (hit == null || hit.getEntity().getId() != message.targetId) {
                return;
            }
            Entity target = hit.getEntity();
            if (!target.isAttackable() || (target instanceof PlayerEntity && !player.canHarmPlayer((PlayerEntity) target))) {
                return;
            }
            player.resetLastActionTime();
            player.attack(target);
        });
        context.setPacketHandled(true);
    }

    private static final class ReachAttack {
        private final int targetId;

        private ReachAttack(int targetId) {
            this.targetId = targetId;
        }
    }
}
