package tfc.dynamicportals.network.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
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

    public SyncLevelsPacket(int viewDistance, int simulationDistance, RegistryAccess.Frozen registryHolder, MinecraftServer server) {
        vd = viewDistance;
        sd = simulationDistance;

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
                    buf.readWithCodec(DimensionType.CODEC),
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
                    buf.readLong()
            ));
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(vd);
        buf.writeInt(sd);

        buf.writeInt(entries.size());
        for (LevelEntry entry : entries) {
            buf.writeBoolean(entry.hardcore);
            buf.writeBoolean(entry.debug);
            buf.writeBoolean(entry.flat);
            buf.writeWithCodec(DimensionType.CODEC, entry.type);
            buf.writeResourceLocation(entry.dimension.location());
            buf.writeLong(entry.seed);
        }
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if (checkClient(ctx)) {
            Minecraft mc = Minecraft.getInstance();
            LevelLoader ldr = ((NetworkHolder) mc).getLoader();
            if (ldr instanceof ClientLevelLoader cldr) {
                cldr.dump();
                for (LevelEntry entry : entries) {
                    cldr.ensure(vd, sd, entry);
                }
                for (PortalNet portalNetwork : ((NetworkHolder) mc).getPortalNetworks()) {
                    portalNetwork.correct((NetworkHolder) mc);
                }
            }
        }
    }

    public final class LevelEntry {
        public final boolean hardcore;
        public final boolean debug, flat;
        public final Holder<DimensionType> type;
        public final ResourceKey<Level> dimension;
        public final long seed;

        public LevelEntry(boolean hardcore, boolean debug, boolean flat, Holder<DimensionType> type, ResourceKey<Level> dimension, long seed) {
            this.hardcore = hardcore;
            this.debug = debug;
            this.flat = flat;
            this.type = type;
            this.dimension = dimension;
            this.seed = seed;
        }
    }
}
