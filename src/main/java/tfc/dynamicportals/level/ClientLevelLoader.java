package tfc.dynamicportals.level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import tfc.dynamicportals.network.sync.SyncLevelsPacket;

import java.util.HashMap;

public class ClientLevelLoader extends LevelLoader {
    Minecraft mc;

    public ClientLevelLoader(Minecraft mc) {
        this.mc = mc;
    }

    //@formatter:off
    HashMap<
            ResourceLocation,
            HashMap<ResourceLocation, Level>
    > levels = new HashMap<>();
    //@formatter:on

    @Override
    public Level get(ResourceKey<Level> world) {
        Level mcLvl = mc.level;
        if (mcLvl != null && world.equals(mcLvl.dimension())) {
            return levels.computeIfAbsent(
                    world.registry(),
                    (k) -> new HashMap<>()
            ).putIfAbsent(
                    world.location(),
                    mcLvl
            );
        }

        return levels.computeIfAbsent(
                world.registry(),
                (k) -> new HashMap<>()
        ).putIfAbsent(
                world.location(),
                null // TODO: custom world loaders
        );
    }

    public void dump() {
        for (HashMap<ResourceLocation, Level> value : levels.values()) {
            for (Level level : value.values()) {
                if (level != null)
                    MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(level));
            }
        }
        levels.clear();
    }

    public void ensure(int vd, int sd, SyncLevelsPacket.LevelEntry entry) {
        ResourceKey<Level> world = entry.dimension;

        Level mcLvl = mc.level;
        if (mcLvl != null && world.equals(mcLvl.dimension())) {
            levels.computeIfAbsent(
                    world.registry(),
                    (k) -> new HashMap<>()
            ).putIfAbsent(
                    world.location(),
                    mcLvl
            );
            return;
        }

        //noinspection resource
        levels.computeIfAbsent(
                world.registry(),
                (k) -> new HashMap<>()
        ).computeIfAbsent(
                world.location(),
                (k) -> {
                    LevelRenderer renderer = new LevelRenderer(mc, mc.renderBuffers());
                    ClientLevel lvl = new ClientLevel(
                            mc.getConnection(),
                            new ClientLevel.ClientLevelData(Difficulty.NORMAL, entry.hardcore, entry.flat),
                            world, entry.type,
                            vd, sd,
                            mc::getProfiler,
                            renderer,
                            entry.debug, entry.seed
                    );
                    renderer.setLevel(lvl);
                    // ClientPacketListener pConnection
                    // ClientLevel.ClientLevelData pClientLevelData
                    // ResourceKey<Level> pDimension
                    // Holder<DimensionType> pDimensionType
                    // int pViewDistance
                    // int pServerSimulationDistance
                    // Supplier<ProfilerFiller> pProfiler
                    // LevelRenderer pLevelRenderer
                    // boolean pIsDebug
                    // long pBiomeZoomSeed
                    return lvl;
                }
        );
    }

    public void update(ClientLevel level) {
        Level old = levels.computeIfAbsent(
                level.dimension().registry(),
                (k) -> new HashMap<>()
        ).get(
                level.dimension().location()
        );
        if (old != null)
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(old));
        levels.computeIfAbsent(
                level.dimension().registry(),
                (k) -> new HashMap<>()
        ).put(
                level.dimension().location(),
                level
        );
    }
}
