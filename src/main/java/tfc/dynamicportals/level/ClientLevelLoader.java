package tfc.dynamicportals.level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import tfc.dynamicportals.network.sync.SyncLevelsPacket;

import java.util.HashMap;
import java.util.Map;

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

    private Map<ResourceLocation, Level> getMap(ResourceLocation key) {
        HashMap<ResourceLocation, Level> mp = levels.get(key);
        if (mp == null) levels.put(key, mp = new HashMap());
        return mp;
    }

    @Override
    public Level get(ResourceKey<Level> world) {
        Level mcLvl = mc.level;
        if (mcLvl != null && world.equals(mcLvl.dimension())) {
            return getMap(world.registry()).putIfAbsent(
                    world.location(),
                    mcLvl
            );
        }

        return getMap(world.registry()).putIfAbsent(
                world.location(),
                null // TODO: custom world loaders
        );
    }

    public void dump() {
        for (HashMap<ResourceLocation, Level> value : levels.values()) {
            for (Level level : value.values()) {
                if (level != null)
                    MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level));
            }
        }
        levels.clear();
    }

    public void ensure(RegistryAccess access, int vd, int sd, SyncLevelsPacket.LevelEntry entry) {
        ResourceKey<Level> world = entry.dimension;

        Level mcLvl = mc.level;
        if (mcLvl != null && world.equals(mcLvl.dimension())) {
            getMap(world.registry()).putIfAbsent(
                    world.location(),
                    mcLvl
            );
            return;
        }

//        if (entry.type.unwrap().right().isPresent()) {
//            System.out.println("Present " + entry.type.unwrap().right().get());
//        }
        //noinspection resource
        getMap(world.registry()).computeIfAbsent(
                world.location(),
                (k) -> {
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
                    LevelRenderer renderer = new LevelRenderer(mc, mc.getEntityRenderDispatcher(), mc.getBlockEntityRenderDispatcher(), mc.renderBuffers());
                    ClientLevel lvl = new ClientLevel(
                            mc.getConnection(),
                            new ClientLevel.ClientLevelData(Difficulty.NORMAL, entry.hardcore, entry.flat),
                            world,
                            access.registry(Registries.DIMENSION_TYPE).get().getHolder(entry.typeKey).orElseThrow(),
                            vd, sd,
                            mc::getProfiler,
                            renderer,
                            entry.debug, entry.seed
                    );
                    renderer.setLevel(lvl);
                    return lvl;
                }
        );
//        System.out.println("Registered");
    }

    public void update(ClientLevel level) {
        Level old = getMap(level.dimension().registry()).get(
                level.dimension().location()
        );
        if (old != null)
            MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(old));
        getMap(level.dimension().registry()).put(
                level.dimension().location(),
                level
        );
    }
}
