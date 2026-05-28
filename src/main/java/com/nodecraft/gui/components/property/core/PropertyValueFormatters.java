package com.nodecraft.gui.components.property.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public final class PropertyValueFormatters {

    private PropertyValueFormatters() {
    }

    public static String formatBlockPos(BlockPos pos) {
        if (pos == null) {
            return "(null)";
        }
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }

    public static String formatVector3d(Vector3d vec) {
        if (vec == null) {
            return "(null)";
        }
        return String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
    }

    public static String formatVec3d(Vec3d vec) {
        if (vec == null) {
            return "(null)";
        }
        return String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
    }
}
