package tfc.dynamicportals.portals;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;

public class PortalPair {
	public BasicPortal left;
	public BasicPortal right;
	
	public PortalPair(BasicPortal left, BasicPortal right) {
		this.left = left;
		this.right = right;
	}
	
	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(left.type.getRegistryName());
		buf.writeNbt(left.serialize());
		
		// has target
		if (right == null || left.equals(right)) {
			buf.writeBoolean(false);
			return;
		}
		
		buf.writeBoolean(true);
		
		buf.writeResourceLocation(right.type.getRegistryName());
		buf.writeNbt(right.serialize());
	}
	
	public void read(FriendlyByteBuf buf) {
		// read source
		ResourceLocation type = buf.readResourceLocation();
		CompoundTag data = buf.readNbt();
		
		Level level = Minecraft.getInstance().level;
		left = BasicPortalTypes.createPortal(level, type, data);
		
		if (!buf.readBoolean()) {
			// no target
			right = left;
			right.target = left;
			return;
		}
		
		// read target
		type = buf.readResourceLocation();
		data = buf.readNbt();
		
		level = Minecraft.getInstance().level;
		right = BasicPortalTypes.createPortal(level, type, data);
		
		// set targets
		left.target = right;
		right.target = left;
	}
	
	public boolean canSee(Player player) {
		if (left != null) {
			if (left.raytraceOffset().distanceTo(
					player.getPosition(0)
			) <= (16 * 4)) { // approx 4 chunks, I think
				return true;
			}
		}
		
		if (right != null) {
			if (right.raytraceOffset().distanceTo(
					player.getPosition(0)
			) <= (16 * 4)) { // approx 4 chunks, I think
				return true;
			}
		}
		
		return false;
	}
}
