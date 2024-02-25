package tfc.dynamicportals.api.implementation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.itf.NetworkHolder;

public class BasicPortal extends AbstractPortal {
    public BasicPortal(Level level) {
        super(level, PortalTypes.BASIC);
    }
    
    protected Quaterniond orientation;
    protected Vec2 size;
    
    public void setOrientation(Quaterniond orientation) {
        this.orientation = orientation;
    }
    
    public void setSize(Vec2 size) {
        this.size = size;
    }
    
    @Override
    public AABB getContainingBox() {
        // TODO: base this off orientation&size
        return new AABB(
                position.x, position.y - 1, position.z - 1,
                position.x, position.y + 1, position.z + 1
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
        tag.putLongArray(
                "orentiation",
                new long[] {
                        Double.doubleToLongBits(orientation.x),
                        Double.doubleToLongBits(orientation.y),
                        Double.doubleToLongBits(orientation.z),
                        Double.doubleToLongBits(orientation.w)
                }
        );
        tag.putLongArray(
                "size",
                new long[] {
                        Double.doubleToLongBits(size.x),
                        Double.doubleToLongBits(size.y)
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
        long[] orentiations = tag.getLongArray("orientation");
        orientation = new Quaterniond(
                Double.longBitsToDouble(orentiations[0]),
                Double.longBitsToDouble(orentiations[1]),
                Double.longBitsToDouble(orentiations[2]),
                Double.longBitsToDouble(orentiations[3])
        );
        long[] sizes = tag.getLongArray("size");
        size = new Vec2(
                sizes[0],
                sizes[1]
        );
    }
}
