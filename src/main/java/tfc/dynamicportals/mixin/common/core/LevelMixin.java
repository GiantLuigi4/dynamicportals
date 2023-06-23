package tfc.dynamicportals.mixin.common.core;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.access.IHoldPortals;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.portals.CommandPortal;
import tfc.dynamicportals.networking.DynamicPortalsNetworkRegistry;
import tfc.dynamicportals.networking.DypoNetworkTargets;
import tfc.dynamicportals.networking.sync.RemovePortalPacket;
import tfc.dynamicportals.util.TrackyTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class LevelMixin implements IHoldPortals {
	@Shadow
	@Nullable
	public abstract ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull);
	
	@Shadow
	public abstract LevelChunk getChunkAt(BlockPos pPos);
	
	private final List<AbstractPortal> newPortals = new ArrayList<>();
	
	private final List<AbstractPortal> allPortals = new ArrayList<>();
	private final List<CommandPortal> cmdPortals = new ArrayList<>();
	
	@Override
	public List<AbstractPortal> getNewPortals() {
		return newPortals;
	}
	
	@Override
	public boolean isNew(AbstractPortal portal) {
		return newPortals.contains(portal);
	}
	
	boolean isServer;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(WritableLevelData p_204149_, ResourceKey p_204150_, Holder p_204151_, Supplier p_204152_, boolean p_204153_, boolean p_204154_, long p_204155_, CallbackInfo ci) {
		//noinspection ConstantConditions
		this.isServer = (Object) this instanceof ServerLevel;
	}
	
	@Override
	public void addPortal(AbstractPortal portal) {
		synchronized (allPortals) {
			allPortals.add(portal);
			
			if (isServer) newPortals.add(portal);
		}
	}
	
	@Override
	public void addCommandPortal(CommandPortal portal) {
		addPortal((AbstractPortal) portal);
		synchronized (cmdPortals) {
			cmdPortals.add(portal);
		}
	}
	
	@Override
	public CommandPortal getCmdPortal(int id) {
		synchronized (cmdPortals) {
			for (CommandPortal cmdPortal : cmdPortals) {
				if (cmdPortal.myId() == id) {
					return cmdPortal;
				}
			}
		}
		return null;
	}
	
	@Override
	public int addPortal(CommandPortal portal) {
		ArrayList<Integer> ints = new ArrayList<>();
		for (CommandPortal cmdPortal : cmdPortals) ints.add(cmdPortal.myId());
		int id = -1;
		int max = 0;
		for (int i = 0; i < ints.size(); i++) {
			max = Math.max(ints.get(i), max);
			if (!ints.contains(i)) {
				id = i;
				break;
			}
		}
		if (ints.size() == 0) max = -1;
		if (id == -1) id = max + 1;
		
		int v = portal.setId(id);
		addCommandPortal(portal);
		if (id != v)
			// TODO: use unsafe to throw unchecked
			// lorenzo: what
			throw new RuntimeException(new IllegalArgumentException("Portal was created with an id of " + v + " even though its id was meant to be " + id));
		return v;
	}
	
	@Override
	public AbstractPortal[] getPortals() {
		synchronized (allPortals) {
			return allPortals.toArray(new AbstractPortal[0]);
		}
	}
	
	@Override
	public CommandPortal[] filter(FullPortalFilter i, CommandContext<?> ctx) {
		return i.filter(List.copyOf(cmdPortals), ctx);
	}
	
	@Override
	public void remove(int myId) {
		synchronized (cmdPortals) {
			for (CommandPortal cmdPortal : cmdPortals) {
				if (cmdPortal.myId() == myId) {
					cmdPortals.remove(cmdPortal);
					AbstractPortal aportal = (AbstractPortal) cmdPortal;
					synchronized (allPortals) {
						allPortals.remove(aportal);
					}
					
					if (aportal instanceof BasicPortal bportal) {
						LevelChunk chunk = getChunkAt(new BlockPos(bportal.raytraceOffset()));
						DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new RemovePortalPacket(bportal.uuid));
						
						if (isServer) TrackyTools.removePortal(((Level) (Object) this), null, bportal);
					}
					
					return;
				}
			}
		}
	}
	
	@Override
	public void remove(UUID uuid) {
		AbstractPortal target = null;
		for (AbstractPortal allPortal : allPortals) {
			if (allPortal.uuid.equals(uuid)) {
				target = allPortal;
				break;
			}
		}
		
		if (isServer) {
			if (target instanceof BasicPortal bap) {
				DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(
						DypoNetworkTargets.TRACKING_PORTAL.with(
								() -> Pair.of(
										(Level) (Object) this,
										bap
								)
						), new RemovePortalPacket(bap.uuid)
				);
				
				TrackyTools.removePortal(((Level) (Object) this), null, bap);
			}
		}
		
		synchronized (allPortals) {
			allPortals.remove(target);
			
			if (target instanceof CommandPortal) {
				synchronized (cmdPortals) {
					cmdPortals.remove((CommandPortal) target);
				}
			}
		}
	}
	
	@Override
	public AbstractPortal getPortal(UUID uuid) {
		synchronized (allPortals) {
			for (AbstractPortal allPortal : allPortals) {
				if (allPortal.uuid.equals(uuid))
					return allPortal;
			}
		}
		return null;
	}
}
