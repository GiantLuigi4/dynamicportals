package tfc.dynamicportals.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.implementation.data.PortalDataTracker;
import tfc.dynamicportals.networking.Packet;

import java.util.UUID;

public class PortalUpdatePacket extends Packet {
	UUID target;
	PortalDataTracker tracker;
	
	public PortalUpdatePacket(BasicPortal portal) {
		target = portal.uuid;
		tracker = portal.tracker;
	}
	
	FriendlyByteBuf buf;
	
	public PortalUpdatePacket(FriendlyByteBuf buf) {
		super(buf);
		target = buf.readUUID();
		this.buf = buf;
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeUUID(target);
		tracker.write(buf);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			Level level = Minecraft.getInstance().level;
			AbstractPortal portal = Temp.getPortal(level, target);
			if (portal instanceof BasicPortal basicPortal)
				basicPortal.tracker.read(buf);
			ctx.setPacketHandled(true);
		}
	}
}
