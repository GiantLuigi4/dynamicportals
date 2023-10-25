package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.network.util.PortalPacketSender;

import java.io.ByteArrayOutputStream;

public abstract class AbstractPortal {
	public Level myLevel;
	PortalType<?> type;
	
	PortalNet connectedNetwork;
	
	public AbstractPortal(PortalType<?> type) {
		this.type = type;
	}
	
	public abstract AABB getNetworkBox();
	
	public void sendPacket(PortalPacketSender sender) {
		AABB netBox = getNetworkBox().inflate(
				myLevel.getServer().getPlayerList().getSimulationDistance() * 16
		);
		netBox = new AABB(
				netBox.minX,
				Double.NEGATIVE_INFINITY,
				netBox.minZ,
				netBox.maxX,
				Double.POSITIVE_INFINITY,
				netBox.maxZ
		);
		
		for (Player player : myLevel.players()) {
			if (player.getBoundingBox().intersects(netBox)) {
				sender.send(player);
			}
		}
	}
	
	/**
	 * if this returns true, the portal will not appear in world
	 * it will only appear in the portal's rosen-bridge if it has one, elsewise, it'll serve as just a marker for the exit for a one-way portal
	 *
	 * @return if the portal should show up in world
	 */
	public boolean exitOnly() {
		return false;
	}
	
	public abstract void write(CompoundTag tag);
	
	public abstract void load(CompoundTag tag);
}