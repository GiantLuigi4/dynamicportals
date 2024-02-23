package tfc.dynamicportals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class Packet implements CustomPacketPayload {
	public Packet() {
	}
	
	public Packet(FriendlyByteBuf buf) {
	}
	
	@Override
	public ResourceLocation id() {
		return new ResourceLocation("dynamic_portals:uber");
	}
	
	public final void write(FriendlyByteBuf buf) {
		buf.writeShort(DypoNetworkRegistry.packetId(this));
		writeData(buf);
	}
	
	public void writeData(FriendlyByteBuf buf) {
	}
	
	public void handle(PlayPayloadContext ctx) {
	}
	
	public boolean checkClient(PlayPayloadContext ctx) {
		return ctx.flow().getReceptionSide().isClient();
	}
	
	public boolean checkServer(PlayPayloadContext ctx) {
		return ctx.flow().getReceptionSide().isServer();
	}
	
	public void respond(PlayPayloadContext ctx, Packet packet) {
		ctx.workHandler().execute(() -> ctx.replyHandler().send(packet));
	}
}