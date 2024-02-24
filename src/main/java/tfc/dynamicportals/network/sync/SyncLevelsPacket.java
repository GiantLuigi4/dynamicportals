package tfc.dynamicportals.network.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.ClientLevelLoader;
import tfc.dynamicportals.level.LevelLoader;
import tfc.dynamicportals.network.Packet;

import java.util.ArrayList;

public class SyncLevelsPacket extends Packet {
	int vd;
	int sd;
	ArrayList<LevelEntry> entries = new ArrayList<>();
	LayeredRegistryAccess<RegistryLayer> access;
	
	public SyncLevelsPacket(int viewDistance, int simulationDistance, LayeredRegistryAccess<RegistryLayer> registryHolder, MinecraftServer server) {
		vd = viewDistance;
		sd = simulationDistance;
		
		this.access = registryHolder;
		
		for (ServerLevel allLevel : server.getAllLevels()) {
			LevelData levelData = allLevel.getLevelData();
			
			entries.add(new LevelEntry(
					levelData.isHardcore(),
					allLevel.isDebug(),
					allLevel.isFlat(),
					allLevel.dimensionTypeRegistration(),
					allLevel.dimension(),
					BiomeManager.obfuscateSeed(allLevel.getSeed())
			));
		}
	}
	
	public SyncLevelsPacket(FriendlyByteBuf buf) {
		super(buf);
		vd = buf.readInt();
		sd = buf.readInt();
		
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			entries.add(new LevelEntry(
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readResourceKey(Registries.DIMENSION_TYPE),
					buf.readResourceKey(Registries.DIMENSION),
					buf.readLong()
			));
		}
	}
	
	@Override
	public void writeData(FriendlyByteBuf buf) {
		buf.writeInt(vd);
		buf.writeInt(sd);
		
		buf.writeInt(entries.size());
		for (LevelEntry entry : entries) {
			buf.writeBoolean(entry.hardcore);
			buf.writeBoolean(entry.debug);
			buf.writeBoolean(entry.flat);
			// don't ask
			buf.writeResourceKey(entry.type.unwrapKey().orElseGet(() ->
					access.getAccessFrom(RegistryLayer.DIMENSIONS)
							.registry(Registries.DIMENSION_TYPE)
							.orElseGet(() -> access
									.getAccessFrom(RegistryLayer.WORLDGEN)
									.registry(Registries.DIMENSION_TYPE)
									.orElseGet(() ->
											access
													.getAccessFrom(RegistryLayer.STATIC)
													.registry(Registries.DIMENSION_TYPE)
													.orElseGet(() ->
															access
																	.getAccessFrom(RegistryLayer.RELOADABLE)
																	.registry(Registries.DIMENSION_TYPE)
																	.orElseThrow()
													)
									))
							.getResourceKey(entry.type.get()).orElseThrow()
			));
			buf.writeResourceKey(entry.dimension);
			buf.writeLong(entry.seed);
		}
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			ctx.enqueueWork(() -> {
				Minecraft mc = Minecraft.getInstance();
				LevelLoader ldr = ((NetworkHolder) mc).getLoader();
				if (ldr instanceof ClientLevelLoader cldr) {
					cldr.dump();
					for (LevelEntry entry : entries) {
						cldr.ensure(
								mc.getConnection().registryAccess(),
								vd, sd, entry
						);
					}
					for (PortalNet portalNetwork : ((NetworkHolder) mc).getPortalNetworks()) {
						portalNetwork.correct((NetworkHolder) mc);
					}
				}
			});
		}
	}
	
	public final class LevelEntry {
		public final boolean hardcore;
		public final boolean debug, flat;
		public final Holder<DimensionType> type;
		public final ResourceKey<DimensionType> typeKey;
		public final ResourceKey<Level> dimension;
		public final long seed;
		
		public LevelEntry(boolean hardcore, boolean debug, boolean flat, Holder<DimensionType> type, ResourceKey<Level> dimension, long seed) {
			this.hardcore = hardcore;
			this.debug = debug;
			this.flat = flat;
			this.type = type;
			typeKey = null;
			this.dimension = dimension;
			this.seed = seed;
		}
		
		public LevelEntry(boolean hardcore, boolean debug, boolean flat, ResourceKey<DimensionType> type, ResourceKey<Level> dimension, long seed) {
			this.hardcore = hardcore;
			this.debug = debug;
			this.flat = flat;
			this.type = null;
			this.typeKey = type;
			this.dimension = dimension;
			this.seed = seed;
		}
	}
}
