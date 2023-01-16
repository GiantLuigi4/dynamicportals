package tfc.dynamicportals.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class Packet implements net.minecraft.network.protocol.Packet {
	public Packet() {
	}
	
	public Packet(FriendlyByteBuf buf) {
	}
	
	public void write(FriendlyByteBuf buf) {
	}
	
	public void handle(NetworkEvent.Context ctx) {
	}
	
	public final void handle(PacketListener pHandler) {
	}
	
	public boolean isSkippable() {
		return net.minecraft.network.protocol.Packet.super.isSkippable();
	}
	
	public boolean checkClient(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isClient();
	}
	
	public boolean checkServer(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isServer();
	}
	
	public void respond(NetworkEvent.Context ctx, Packet packet) {
		ctx.enqueueWork(() -> {
			if (checkClient(ctx)) DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.sendToServer(packet);
			else DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), packet);
		});
	}
}
