package tfc.dynamicportals.network.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.Packet;

public class CreateNetworkPacket extends Packet {
	PortalNet net;
	CompoundTag tg;

	public CreateNetworkPacket(PortalNet net) {
		this.net = net;
	}
	
	public CreateNetworkPacket(FriendlyByteBuf buf) {
		tg = buf.readNbt();
		net = new PortalNet(tg.getUUID("uuid"));
	}
	
	@Override
	public void writeData(FriendlyByteBuf buf) {
		CompoundTag tg = new CompoundTag();
		net.write(tg);
		buf.writeNbt(tg);
	}
	
	@Override
	public void handle(PlayPayloadContext ctx) {
		if (checkClient(ctx)) {
			net.read((NetworkHolder) Minecraft.getInstance(), (ListTag) tg.get("data"));
			((NetworkHolder) Minecraft.getInstance()).getPortalNetworks().add(net);
		}
	}
}
