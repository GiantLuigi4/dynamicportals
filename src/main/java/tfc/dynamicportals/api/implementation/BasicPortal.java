package tfc.dynamicportals.api.implementation;

import com.mojang.math.Quaternion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.itf.NetworkHolder;

public class BasicPortal extends AbstractPortal {
    Vec2 size;

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
        tag.putLongArray(
                "coords",
                new long[]{
                        Double.doubleToLongBits(position.x),
                        Double.doubleToLongBits(position.y),
                        Double.doubleToLongBits(position.z)
                }
        );
        tag.putIntArray(
                "orientation",
                new int[]{
                        Float.floatToIntBits(orientation.i()),
                        Float.floatToIntBits(orientation.j()),
                        Float.floatToIntBits(orientation.k()),
                        Float.floatToIntBits(orientation.r())
                }
        );
        tag.putIntArray(
                "size",
                new int[]{
                        Float.floatToIntBits(size.x),
                        Float.floatToIntBits(size.y)
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

        try {
            int[] orient = tag.getIntArray("orientation");
            orientation = new Quaternion(
                    Float.intBitsToFloat(orient[0]),
                    Float.intBitsToFloat(orient[1]),
                    Float.intBitsToFloat(orient[2]),
                    Float.intBitsToFloat(orient[3])
            );
        } catch (Throwable err) {
            orientation = new Quaternion(0, 0, 0, 1f);
        }

        try {
            int[] size = tag.getIntArray("size");
            this.size = new Vec2(
                    Float.intBitsToFloat(size[0]),
                    Float.intBitsToFloat(size[1])
            );
        } catch (Throwable err) {
            this.size = new Vec2(2, 2);
        }
    }

    public Vec2 getSize() {
        return size;
    }
}
