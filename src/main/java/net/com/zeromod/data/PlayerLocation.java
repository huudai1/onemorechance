package net.com.zeromod.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * (yaw, pitch) player.
 */
public class PlayerLocation {
    private final Vec3 position;
    private final ResourceKey<Level> dimension;
    private final float yaw;
    private final float pitch;

    public PlayerLocation(Vec3 position, ResourceKey<Level> dimension, float yaw, float pitch) {
        this.position = position;
        this.dimension = dimension;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vec3 getPosition() {
        return position;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}