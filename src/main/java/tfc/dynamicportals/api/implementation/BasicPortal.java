package tfc.dynamicportals.api.implementation;

import com.mojang.math.Quaternion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;

import java.io.ByteArrayOutputStream;

public class BasicPortal extends AbstractPortal {
    public BasicPortal(PortalType<?> type) {
        super(type);
    }

    public BasicPortal() {
        super(BasicPortalTypes.BASIC);
    }

    @Override
    public AABB getNetworkBox() {
        return new AABB(
                position.x - 1, position.y - 1, position.z - 1,
                position.x + 1, position.y + 1, position.z + 1
        );
    }

    @Override
    public void write(CompoundTag tag) {
        // I got lazy
        tag.putLongArray(
                "coords",
                new long[]{
                        Double.doubleToLongBits(position.x),
                        Double.doubleToLongBits(position.y),
                        Double.doubleToLongBits(position.z)
                }
        );
    }

    @Override
    public void load(CompoundTag tag) {
        long[] coords = tag.getLongArray("coords");
        position = new Vec3(
                Double.longBitsToDouble(coords[0]),
                Double.longBitsToDouble(coords[1]),
                Double.longBitsToDouble(coords[2])
        );
    }
}
