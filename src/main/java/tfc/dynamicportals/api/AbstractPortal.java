package tfc.dynamicportals.api;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.client.AbstractPortalRenderDispatcher;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.mixin.client.data.access.ClientLevelAccessor;
import tfc.dynamicportals.network.util.PortalPacketSender;

public abstract class AbstractPortal {
    public final Level myLevel;
    // highly double it's possible to have a portal implementation which doesn't need to do stuff upon moving
    // thus, this field is encapsulated
    protected Vec3 position;
    public final PortalType<?> type;

    PortalNet connectedNetwork;

    public AbstractPortal(Level level, PortalType<?> type) {
        this.myLevel = level;
        this.type = type;
    }

    public AABB getNetworkBox() {
        return getContainingBox();
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

    public void setPosition(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
    }

    // TODO: portal renderer class
    /* DISCOURAGED */
    public AbstractPortalRenderDispatcher preferredDispatcher() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public LevelRenderer getTargetLevelRenderer() {
        if (connectedNetwork == null) return null;
        if (connectedNetwork.portals.size() == 2) {
            AbstractPortal left = connectedNetwork.getPortals().get(0);
            return ((ClientLevelAccessor) (left == this ? connectedNetwork.getPortals().get(1) : left).myLevel).getLevelRenderer();
        }
        return null;
    }
}