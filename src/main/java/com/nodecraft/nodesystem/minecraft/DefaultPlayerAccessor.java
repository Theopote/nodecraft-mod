package com.nodecraft.nodesystem.minecraft;

import com.nodecraft.nodesystem.util.Vector3;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Default live accessor backed by the current world/player references inside execution context.
 */
public class DefaultPlayerAccessor implements PlayerAccessor {

    private static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
    private static final String UNKNOWN = "minecraft:unknown";

    private final World contextWorld;
    @Nullable
    private final ServerPlayerEntity player;

    public DefaultPlayerAccessor(World contextWorld, @Nullable ServerPlayerEntity player) {
        this.contextWorld = contextWorld;
        this.player = player;
    }

    @Override
    public Vector3 getPlayerPosition() {
        if (player == null) {
            return ZERO;
        }
        return new Vector3((float) player.getX(), (float) player.getY(), (float) player.getZ());
    }

    @Override
    public Vector3 getPlayerEyePosition() {
        if (player == null) {
            return ZERO;
        }
        return new Vector3((float) player.getX(), (float) player.getEyeY(), (float) player.getZ());
    }

    @Override
    public Vector3 getPlayerLookVector() {
        if (player == null) {
            return ZERO;
        }
        Vec3d look = player.getRotationVec(1.0f).normalize();
        return new Vector3((float) look.x, (float) look.y, (float) look.z);
    }

    @Override
    public String getPlayerDimension() {
        World source = resolveWorld();
        if (source == null) {
            return UNKNOWN;
        }
        return source.getRegistryKey().getValue().toString();
    }

    @Override
    public String getPlayerBiome() {
        World source = resolveWorld();
        if (source == null) {
            return UNKNOWN;
        }

        BlockPos samplePos = player != null ? player.getBlockPos() : BlockPos.ORIGIN;
        return source.getBiome(samplePos)
            .getKey()
            .map(RegistryKey::getValue)
            .map(Object::toString)
            .orElse(UNKNOWN);
    }

    @Override
    public long getWorldTime() {
        World source = resolveWorld();
        return source != null ? source.getTimeOfDay() : 0L;
    }

    @Override
    public int getWorldDay() {
        return (int) (getWorldTime() / 24000L);
    }

    @Override
    public boolean isDaytime() {
        World source = resolveWorld();
        return source != null && source.isDay();
    }

    @Override
    public boolean isRaining() {
        World source = resolveWorld();
        return source != null && source.isRaining();
    }

    @Override
    public boolean isThundering() {
        World source = resolveWorld();
        return source != null && source.isThundering();
    }

    private @Nullable World resolveWorld() {
        World playerWorld = tryResolvePlayerWorld();
        if (playerWorld != null) {
            return playerWorld;
        }
        return contextWorld;
    }

    private @Nullable World tryResolvePlayerWorld() {
        if (player == null) {
            return null;
        }

        World resolved = invokeWorldAccessor(player, "getServerWorld");
        if (resolved != null) {
            return resolved;
        }
        return invokeWorldAccessor(player, "getWorld");
    }

    private @Nullable World invokeWorldAccessor(ServerPlayerEntity source, String methodName) {
        try {
            Method method = source.getClass().getMethod(methodName);
            Object value = method.invoke(source);
            if (value instanceof World world) {
                return world;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
}
