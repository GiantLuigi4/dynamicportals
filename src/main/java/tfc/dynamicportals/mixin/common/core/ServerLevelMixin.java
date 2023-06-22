package tfc.dynamicportals.mixin.common.core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.IHoldPortals;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.networking.DynamicPortalsNetworkRegistry;
import tfc.dynamicportals.networking.DypoNetworkTargets;
import tfc.dynamicportals.networking.sync.PortalUpdatePacket;
import tfc.dynamicportals.networking.sync.SpawnPortalPacket;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(at = @At("TAIL"), method = "tick")
	public void postTick(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
		Level level = (Level) (Object) this;
		IHoldPortals portalHolder = (IHoldPortals) level;
		
		for (AbstractPortal portal : Temp.getPortals(level)) {
			if (portal instanceof BasicPortal bap) {
				if (bap.tracker.isDirty()) {
					if (portalHolder.isNew(bap)) {
						bap.tracker.write(null);
					} else {
						LevelChunk chunk = level.getChunkAt(new BlockPos(bap.raytraceOffset()));
						DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(
								PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
								new PortalUpdatePacket(bap)
						);
					}
				}
			}
		}
		
		ArrayList<AbstractPortal> sent = new ArrayList<>();
		for (AbstractPortal portal : portalHolder.getNewPortals()) {
			if (portal instanceof BasicPortal bap) {
				DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(
						DypoNetworkTargets.TRACKING_PORTAL.with(
								() -> Pair.of(
										(Level) (Object) this,
										bap
								)
						), new SpawnPortalPacket(bap)
				);
			}
		}
		
		portalHolder.getNewPortals().clear();
	}
}
