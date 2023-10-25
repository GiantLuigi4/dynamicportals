package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.util.PortalPacketSender;
import tfc.dynamicportals.util.ReadOnlyList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PortalNet {
    int latestPortalId = 0;

    ArrayList<AbstractPortal> portals = new ArrayList<>();
    ReadOnlyList<AbstractPortal> readOnly = new ReadOnlyList<>(portals);

    public List<AbstractPortal> getPortals() {
        return readOnly;
    }

    UUID uuid;

    public PortalNet(UUID uuid) {
        this.uuid = uuid;
    }

    public void link(AbstractPortal portal) {
        if (portal.connectedNetwork != null) {
            portal.connectedNetwork.unlink(portal);
        }
        portals.add(portal);
        portal.connectedNetwork = this;
    }

    private void unlink(AbstractPortal portal) {
        if (portal.connectedNetwork == this) {
            portal.connectedNetwork = null;
            portals.remove(portal);
        }
    }

    public void sendPacket(PortalPacketSender sender) {
        for (AbstractPortal portal : portals) {
            portal.sendPacket(sender);
        }
    }

    public void write(CompoundTag tag) {
        ListTag tags = new ListTag();
        for (AbstractPortal portal : portals) {
            CompoundTag tg = new CompoundTag();
            portal.write(tg);
            tags.add(tg);
            tg.putString("type", portal.type.getRegistryName().toString());
            CompoundTag world = new CompoundTag();
            world.putString("registry", portal.myLevel.dimension().registry().toString());
            world.putString("location", portal.myLevel.dimension().location().toString());
            tg.put("level", world);
        }
        tag.putUUID("uuid", uuid);
        tag.put("data", tags);
    }

    public void read(NetworkHolder holder, ListTag data) {
        for (Tag datum : data) {
            CompoundTag tg = (CompoundTag) datum;
            portals.add(
                    BasicPortalTypes.createPortal(
                            new ResourceLocation(tg.getString("type")), holder, tg
                    )
            );
        }
    }

    // writes and reads own data to correct level references
    public void correct(NetworkHolder holder) {
        CompoundTag tg = new CompoundTag();
        write(tg);
        portals.clear();
        read(holder, tg.getList("data", Tag.TAG_COMPOUND));
    }
}
