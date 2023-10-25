package tfc.dynamicportals.api.implementation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.itf.NetworkHolder;

public class BasicPortal extends AbstractPortal {
    public BasicPortal(Level level, PortalType<?> type) {
        super(level, type);
    }

    public BasicPortal(Level level) {
        super(level, BasicPortalTypes.BASIC);
    }

    @Override
    public AABB getContainingBox() {
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
    public void load(NetworkHolder holder, CompoundTag tag) {
        long[] coords = tag.getLongArray("coords");
        position = new Vec3(
                Double.longBitsToDouble(coords[0]),
                Double.longBitsToDouble(coords[1]),
                Double.longBitsToDouble(coords[2])
        );
    }
}
