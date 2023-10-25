package tfc.dynamicportals.level;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class LevelLoader {
    public abstract Level get(ResourceKey<Level> world);
}
