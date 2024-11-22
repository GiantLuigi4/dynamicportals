package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.client.AbstractPortalRenderDispatcher;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.util.PortalPacketSender;

import java.util.UUID;

public abstract class AbstractPortal {
    public final Level myLevel;
    // highly doubt it's possible to have a portal implementation which doesn't need to do stuff upon moving
    // thus, this field is encapsulated
    protected Vec3 position;
    protected Quaterniond orientation;
    public final PortalType<?> type;

    PortalNet connectedNetwork;

    public AbstractPortal(Level level, PortalType<?> type) {
        this.myLevel = level;
        this.type = type;
    }

    public AABB getNetworkBox() {
        return getContainingBox().inflate(100);
    }

    public abstract AABB getContainingBox();

    public void sendPacket(PortalPacketSender sender) {
        AABB netBox = getNetworkBox().inflate(
                myLevel.getServer().getPlayerList().getSimulationDistance() * 16
        );
        netBox = new AABB(
                netBox.minX,
                Double.NEGATIVE_INFINITY,
                netBox.minZ,
                netBox.maxX,
                Double.POSITIVE_INFINITY,
                netBox.maxZ
        );

        for (Player player : myLevel.players()) {
            if (player.getBoundingBox().intersects(netBox)) {
                sender.send(player);
            }
        }
    }

    /**
     * if this returns true, the portal will not appear in world
     * it will only appear in the portal's rosen-bridge if it has one, elsewise, it'll serve as just a marker for the exit for a one-way portal
     *
     * @return if the portal should show up in world
     */
    public boolean exitOnly() {
        return false;
    }

    public abstract void write(CompoundTag tag);

    public abstract void load(NetworkHolder holder, CompoundTag tag);

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 vec) {
        this.position = vec;
    }

    public Quaterniond getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaterniond orientation) {
        this.orientation = orientation;
    }

    public PortalNet getConnectedNetwork() {
        return connectedNetwork;
    }

    public void setPosition(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
    }

    /**
     * Gets the interpolation delta to the portal, should return -1 if the ray does not intersect
     *
     * @param start  the start vector (i.e. the player's camera)
     * @param end    the end vector (either the coordinate of the end of the reach vector, or the coordinate of a block hit result)
     * @param temp   a temporary vector3d to use for math calculations
     * @param length the distance between start and end
     * @return the interpolation delta, or -1 for no hit
     */
    public abstract double trace(Vec3 start, Vec3 end, Vector3d temp, double length);

    /* DISCOURAGED */
    public AbstractPortalRenderDispatcher preferredDispatcher() {
        return null;
    }

    public abstract Vec3 transformVec(Vec3 from, Vector3d temp, boolean isTarget);

    public abstract boolean wasCrossed(AABB bounds, Vec3 motion, Vector3d temp);

    public abstract double distanceAlong(AABB box, Vec3 motion, Vector3d temp);
}