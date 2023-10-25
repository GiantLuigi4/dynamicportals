package tfc.dynamicportals.level;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class ServerLevelLoader extends LevelLoader {
    MinecraftServer server;

    @Override
    public Level get(ResourceKey<Level> world) {
        return server.getLevel(world);
    }
}
