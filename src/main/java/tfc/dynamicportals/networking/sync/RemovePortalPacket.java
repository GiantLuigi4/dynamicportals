package tfc.dynamicportals.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.networking.Packet;

import java.util.UUID;

public class RemovePortalPacket extends Packet {
	UUID toRemove;
	
	public RemovePortalPacket(UUID uuid) {
		toRemove = uuid;
	}
	
	public RemovePortalPacket(FriendlyByteBuf buf) {
		super(buf);
		toRemove = buf.readUUID();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeUUID(toRemove);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		super.handle(ctx);
		if (checkClient(ctx)) {
			Level lvl = Minecraft.getInstance().level;
			Temp.remove(lvl, toRemove);
		}
	}
}
