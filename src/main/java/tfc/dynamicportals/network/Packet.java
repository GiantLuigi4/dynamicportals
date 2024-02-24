package tfc.dynamicportals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class Packet {
	public Packet() {
	}
	
	public Packet(FriendlyByteBuf buf) {
	}
	
	public void writeData(FriendlyByteBuf buf) {
	}
	
	public void handle(NetworkEvent.Context ctx) {
	}
	
	public boolean checkClient(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isClient();
	}
	
	public boolean checkServer(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isServer();
	}
	
	public void respond(NetworkEvent.Context ctx, Packet packet) {
		ctx.enqueueWork(() -> {
			if (checkServer(ctx))
				DypoNetworkRegistry.send(packet, PacketDistributor.PLAYER.with(ctx::getSender));
			else DypoNetworkRegistry.sendToServer(packet);
		});
	}
	
	// for 1.20.4
	public ResourceLocation id() {
		return new ResourceLocation("dynamic_portals:uber");
	}
	
	public final void write(FriendlyByteBuf buf) {
		buf.writeShort(DypoNetworkRegistry.packetId(this));
		writeData(buf);
	}
}
