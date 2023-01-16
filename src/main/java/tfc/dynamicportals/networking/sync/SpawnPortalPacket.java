package tfc.dynamicportals.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.networking.Packet;

public class SpawnPortalPacket extends Packet {
	ResourceLocation type;
	CompoundTag tag;
	
	public SpawnPortalPacket(BasicPortal portal) {
		type = portal.type.getRegistryName();
		tag = portal.serialize();
	}
	
	public SpawnPortalPacket(FriendlyByteBuf buf) {
		super(buf);
		type = buf.readResourceLocation();
		tag = buf.readNbt();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeResourceLocation(type);
		buf.writeNbt(tag);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			// TODO: defer portal creation so that pairs can exist
			Level level = Minecraft.getInstance().level;
			BasicPortal portal = BasicPortalTypes.createPortal(level, type, tag);
			Temp.addRegularPortal(level, portal);
			ctx.setPacketHandled(true);
		}
	}
}
