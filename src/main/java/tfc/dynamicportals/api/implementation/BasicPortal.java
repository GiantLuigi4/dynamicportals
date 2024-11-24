package tfc.dynamicportals.api.implementation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector2d;
import org.joml.Vector3d;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.itf.NetworkHolder;

public class BasicPortal extends AbstractPortal {
    public BasicPortal(Level level) {
        super(level, PortalTypes.BASIC);
    }

    public BasicPortal(Level level, PortalType<BasicPortal> type) {
        super(level, type);
    }

    protected Vector2d size;
    protected boolean doubleSided = true;

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
                "orientation",
                new long[]{
                        Double.doubleToLongBits(orientation.x),
                        Double.doubleToLongBits(orientation.y),
                        Double.doubleToLongBits(orientation.z),
                        Double.doubleToLongBits(orientation.w)
                }
        );
        tag.putLongArray(
                "size",
                new long[]{
                        Double.doubleToLongBits(size.x),
                        Double.doubleToLongBits(size.y)
                }
        );
        tag.putBoolean("double_sided", this.doubleSided);
    }

    @Override
    public void load(NetworkHolder holder, CompoundTag tag) {
        long[] coords = tag.getLongArray("coords");
        position = new Vec3(
                Double.longBitsToDouble(coords[0]),
                Double.longBitsToDouble(coords[1]),
                Double.longBitsToDouble(coords[2])
        );
        long[] orientations = tag.getLongArray("orientation");
        orientation = new Quaterniond(
                Double.longBitsToDouble(orientations[0]),
                Double.longBitsToDouble(orientations[1]),
                Double.longBitsToDouble(orientations[2]),
                Double.longBitsToDouble(orientations[3])
        );
        long[] sizes = tag.getLongArray("size");
        size = new Vector2d(
                Double.longBitsToDouble(sizes[0]),
                Double.longBitsToDouble(sizes[1])
        );
        this.doubleSided = tag.getBoolean("double_sided");
    }

    public Vector2d getSize() {
        return size;
    }

    public void setSize(Vector2d size) {
        this.size = size;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    @Override
    public double trace(Vec3 start, Vec3 end, Vector3d temp, double length) {
        temp.set(1, 0, 0);
        orientation.transform(temp);

        double dotPos = temp.dot(position.x, position.y, position.z);
        double dotStart = temp.dot(start.x, start.y, start.z);
        double dotEnd = temp.dot(end.x, end.y, end.z);
        double dotLength = dotEnd - dotStart;

        double delta = (dotPos - dotStart) / dotLength;
        if (delta > 0 && delta <= length) {
            Vec3 deltaVec = end.subtract(start);
            Vec3 transformed = start.add(deltaVec.scale(delta));

            // get right
            temp.set(0, 0, 1);
            orientation.transform(temp);
            double offZ = temp.dot(transformed.x, transformed.y, transformed.z);
            dotPos = temp.dot(position.x, position.y, position.z);
            if (Math.abs(offZ - dotPos) * 2.0f > size.x) {
                return -1;
            }

            // get up
            temp.set(0, 1, 0);
            orientation.transform(temp);
            double offY = temp.dot(transformed.x, transformed.y, transformed.z);
            dotPos = temp.dot(position.x, position.y, position.z);
            if (Math.abs(offY - dotPos) * 2.0f > size.y) {
                return -1;
            }

            return delta;
        }

        return -1;
    }

    // TODO: better testing
    @Override
    public Vec3 transformVec(Vec3 from, Vector3d temp, boolean isTarget) {
        if (!isTarget) {
            temp.set(from.x, from.y, from.z);
            temp.sub(position.x, position.y, position.z);
            orientation.transform(temp);
            temp.mul(1, 1. / size.y, 1. / size.x);
            return new Vec3(temp.x, temp.y, temp.z);
        } else {
            temp.set(from.x, from.y, from.z);
            temp.mul(1, size.y, size.x);
            new Quaterniond()
                    .setAngleAxis(Math.toRadians(180), 0, 1, 0)
                    .mul(orientation)
                    .transform(temp);
            temp.add(position.x, position.y, position.z);
            return new Vec3(temp.x, temp.y, temp.z);
        }
    }

    // TODO: further testing
    @Override
    public boolean wasCrossed(AABB bounds, Vec3 motion, Vector3d temp) {
        Vec3 center = bounds.getCenter();
        temp.set(1, 0, 0);
        orientation.transform(temp);
        double dotCenter = temp.dot(center.x, center.y, center.z);
        double dotPos = temp.dot(position.x, position.y, position.z);
        double dotMot = temp.dot(motion.x, motion.y, motion.z);
        double dist0 = dotCenter - dotPos;
        double dist1 = (dotCenter + dotMot) - dotPos;
        if (Math.signum(dist0) != Math.signum(dist1)) {
            double maxPad =
                    // pretty sure this is sufficient computation
                    // TODO: test!
                    Math.max(1, bounds.getXsize()) *
                            Math.max(1, bounds.getYsize()) *
                            Math.max(1, bounds.getZsize()) + 1;

            // right/left
            temp.set(0, 0, 1);
            orientation.transform(temp);
            dotCenter = temp.dot(center.x, center.y, center.z);
            dotPos = temp.dot(position.x, position.y, position.z);
            dotMot = temp.dot(motion.x, motion.y, motion.z);
            double diff = (dotCenter + dotMot) - dotPos;
            if (Math.abs(diff) > maxPad) return false;
            if (Math.abs(diff) > size.x * 0.5) diff = size.x * 0.5 * Math.signum(diff);
            temp.mul(diff).add(position.x, position.y, position.z);

            // up/down
            Vector3d temp2 = new Vector3d(0, 1, 0);
            orientation.transform(temp2);
            dotCenter = temp2.dot(center.x, center.y, center.z);
            dotPos = temp2.dot(position.x, position.y, position.z);
            dotMot = temp2.dot(motion.x, motion.y, motion.z);
            diff = (dotCenter + dotMot) - dotPos;
            if (Math.abs(diff) > maxPad) return false;
            if (Math.abs(diff) > size.y * 0.5) diff = size.y * 0.5 * Math.signum(diff);
            temp.add(temp2.mul(diff));

            return bounds.contains(temp.x, temp.y, temp.z);
        }
        return false;
    }

    @Override
    public double distanceAlong(AABB box, Vec3 motion, Vector3d temp) {
        temp.set(1, 0, 0);
        orientation.transform(temp);

        Vec3 start = box.getCenter();
        Vec3 end = start.add(motion);

        double dotPos = temp.dot(position.x, position.y, position.z);
        double dotStart = temp.dot(start.x, start.y, start.z);
        double dotEnd = temp.dot(end.x, end.y, end.z);
        double dotLength = dotEnd - dotStart;

        double delta = (dotPos - dotStart) / dotLength;
        temp.set(motion.x * delta, motion.y * delta, motion.z * delta);
        return temp.length();
    }

    public void teleport(Entity entity) {
        Vector3d temp = new Vector3d();
        Vec3 tmp = transformVec(entity.position(), temp, false);
        tmp = getConnectedNetwork().transformTarget(this, tmp, temp);
        entity.moveTo(tmp);
        // TODO: rotate entity
        // TODO: funky camera logic for players
    }
}
