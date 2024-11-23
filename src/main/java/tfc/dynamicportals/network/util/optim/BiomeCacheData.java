package tfc.dynamicportals.network.util.optim;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;

import java.lang.ref.WeakReference;

public final class BiomeCacheData {
    public final WeakReference<Holder<Biome>> prevBiome;
    public final int prevPosX;
    public final int prevPosY;
    public final int prevPosZ;

    public BiomeCacheData() {
        prevBiome = new WeakReference<>(null);
        prevPosX = Integer.MAX_VALUE;
        prevPosY = Integer.MAX_VALUE;
        prevPosZ = Integer.MAX_VALUE;
    }

    public BiomeCacheData(
            int prevX, int prevY, int prevZ,
            WeakReference<Holder<Biome>> biome
    ) {
        this.prevPosX = prevX;
        this.prevPosY = prevY;
        this.prevPosZ = prevZ;
        this.prevBiome = biome;
    }

    public Holder<Biome> check(int pX, int pY, int pZ) {
        if (QuartPos.toSection(pX) == prevPosX) {
            if (QuartPos.toSection(pY) == prevPosY) {
                if (QuartPos.toSection(pZ) == prevPosZ) {
                    return prevBiome.get();
                }
            }
        }
        return null;
    }
}
