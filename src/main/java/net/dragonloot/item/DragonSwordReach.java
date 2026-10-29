package net.dragonloot.item;

import net.dragonloot.init.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public final class DragonSwordReach {
    private DragonSwordReach() {
    }

    public static double baseRange(PlayerEntity player) {
        return player.isCreative() ? 6.0D : 3.0D;
    }

    @Nullable
    public static EntityRayTraceResult pick(PlayerEntity player, float partialTick) {
        if (!player.isAlive() || player.isSpectator() || !(player.getMainHandItem().getItem() instanceof DragonSwordItem)) {
            return null;
        }
        double bonus = player.getAttributeValue(ModAttributes.ATTACK_RANGE_BONUS.get());
        if (bonus <= 0.0D) {
            return null;
        }
        double range = baseRange(player) + bonus;
        Vector3d start = player.getEyePosition(partialTick);
        Vector3d direction = player.getViewVector(partialTick).scale(range);
        Vector3d end = start.add(direction);
        RayTraceResult block = player.level.clip(new RayTraceContext(start, end,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
        double limit = block.getType() == RayTraceResult.Type.MISS ? range * range : start.distanceToSqr(block.getLocation());
        EntityRayTraceResult closest = null;
        // ProjectileHelper's player-picking overload is stripped from dedicated servers in 1.16.5.
        for (var entity : player.level.getEntities(player, player.getBoundingBox().expandTowards(direction).inflate(1.0D),
                candidate -> candidate.isAlive() && !candidate.isSpectator() && candidate.isPickable())) {
            if (entity.getRootVehicle() == player.getRootVehicle() && !entity.canRiderInteract()) {
                continue;
            }
            var bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
            var intersection = bounds.clip(start, end);
            if (bounds.contains(start)) {
                closest = new EntityRayTraceResult(entity, start);
                limit = 0.0D;
            } else if (intersection.isPresent()) {
                double distance = start.distanceToSqr(intersection.get());
                if (distance < limit) {
                    closest = new EntityRayTraceResult(entity, intersection.get());
                    limit = distance;
                }
            }
        }
        return closest;
    }
}
