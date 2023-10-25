package tfc.dynamicportals.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.util.PortalPacketSender;
import tfc.dynamicportals.util.ReadOnlyList;

import java.io.ByteArrayOutputStream;
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
        }
        tag.putUUID("uuid", uuid);
        tag.put("data", tags);
    }
    
    public void read(NetworkHolder holder, ListTag data) {
        for (Tag datum : data) {
            CompoundTag tg = (CompoundTag) datum;
            portals.add(
                    BasicPortalTypes.createPortal(
                            new ResourceLocation(tg.getString("type")), tg
                    )
            );
        }
    }
}
