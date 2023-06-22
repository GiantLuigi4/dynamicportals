package tfc.dynamicportals.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.networking.Packet;
import tfc.dynamicportals.portals.PortalPair;

import java.util.UUID;

public class SpawnPortalPacket extends Packet {
	PortalPair pair;
	UUID partner;
	
	public SpawnPortalPacket(BasicPortal portal) {
		this.pair = new PortalPair(portal, portal);
		partner = portal.target.uuid;
	}
	
	public SpawnPortalPacket(PortalPair pair) {
		this.pair = pair;
	}
	
	public SpawnPortalPacket(FriendlyByteBuf buf) {
		super(buf);
		this.pair = new PortalPair(null, null);
		pair.read(buf);
		partner = buf.readUUID();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		pair.write(buf);
		buf.writeUUID(partner);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			// TODO: defer portal creation so that pairs can exist
			Level level = Minecraft.getInstance().level;
			Temp.addRegularPortal(level, pair.left);
			if (pair.right != pair.left) Temp.addRegularPortal(level, pair.right);
			pair.right.target = pair.left;
			AbstractPortal ptrl = Temp.getPortal(level, partner);
			if (ptrl != null) pair.left.target = ptrl;
			pair.left.target.target = pair.left;
			ctx.setPacketHandled(true);
		}
	}
}
