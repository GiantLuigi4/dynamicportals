package tfc.dynamicportals.networking;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.portals.PortalPair;

public class DypoNetworkTargets {
	// TODO: get rid of the Level
	public static PacketDistributor<Pair<Level, PortalPair>> TRACKING_PAIR = new PacketDistributor<>(
			(distributor, supplier) -> packet -> {
				for (Player player : supplier.get().getFirst().players()) {
					if (supplier.get().getSecond().canSee(player)) {
						((ServerPlayer) player).connection.connection.send(packet);
					}
				}
			},
			NetworkDirection.PLAY_TO_CLIENT
	);
	
	// TODO: get rid of the Level
	public static PacketDistributor<Pair<Level, BasicPortal>> TRACKING_PORTAL = new PacketDistributor<>(
//			(distributor, supplier) -> packet -> {
//				for (Player player : supplier.get().getFirst().players()) {
//					if (supplier.get().getSecond().raytraceOffset().distanceTo(player.getPosition(0)) <= (16 * 4)) {
//						((ServerPlayer) player).connection.connection.send(packet);
//					}
//				}
//			},
			(distributor, supplier) -> packet -> {
				Vec3 pos = supplier.get().getSecond().raytraceOffset();
				PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) supplier.get().getFirst().getChunk(
						new BlockPos(
								pos.x, pos.y, pos.z
						)
				)).send(packet);
			},
			NetworkDirection.PLAY_TO_CLIENT
	);
}
