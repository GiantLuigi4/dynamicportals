package tfc.dynamicportals.level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.level.LevelLoader;

import java.util.HashMap;

public class ClientLevelLoader extends LevelLoader {
    Minecraft mc;

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
            ).computeIfAbsent(
                    world.getRegistryName(),
                    (k) -> mcLvl
            );
        }

        // TODO: should probably have some handshake packet?
//        return levels.computeIfAbsent(
//                world.registry(),
//                (k) -> new HashMap<>()
//        ).computeIfAbsent(
//                world.getRegistryName(),
//                (k) -> {
//                    // ClientPacketListener pConnection
//                    // ClientLevel.ClientLevelData pClientLevelData
//                    // ResourceKey<Level> pDimension
//                    // Holder<DimensionType> pDimensionType
//                    // int pViewDistance
//                    // int pServerSimulationDistance
//                    // Supplier<ProfilerFiller> pProfiler
//                    // LevelRenderer pLevelRenderer
//                    // boolean pIsDebug
//                    // long pBiomeZoomSeed
//                    return new ClientLevel(
//                            mc.getConnection(),
//                            new ClientLevel.ClientLevelData(Difficulty.NORMAL, mcLvl.getLevelData().isHardcore(), false),
//                            world, Registry.REGISTRY.get(Registry.DIMENSION_TYPE_REGISTRY.location()).get(world.location()),
//                            mcLvl.getChunkSource()
//                    )
//                }
//        );
        return null;
    }
}
