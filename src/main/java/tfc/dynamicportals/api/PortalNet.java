package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import tfc.dynamicportals.network.util.PortalPacketSender;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PortalNet {
    ArrayList<AbstractPortal> portals = new ArrayList<>();

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

    public ListTag write(CompoundTag tag) {
        ListTag tags = new ListTag();
        for (AbstractPortal portal : portals) {
            CompoundTag tg = new CompoundTag();
            portal.write(tg);
            tags.add(tg);
            tg.putString("type", portal.type.getRegistryName().toString());
        }
        return tags;
    }
}
