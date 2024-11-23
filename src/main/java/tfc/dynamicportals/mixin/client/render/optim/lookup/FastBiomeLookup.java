package tfc.dynamicportals.mixin.client.render.optim.lookup;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.network.util.optim.BiomeCacheData;

import java.lang.ref.WeakReference;

@Mixin(BiomeManager.class)
public class FastBiomeLookup {
    @Unique
    private BiomeCacheData data = new BiomeCacheData();

    @Inject(at = @At("HEAD"), method = "getNoiseBiomeAtQuart", cancellable = true)
    public final void accelerateLookup(int pX, int pY, int pZ, CallbackInfoReturnable<Holder<Biome>> cir) {
        Holder<Biome> biome = data.check(pX, pY, pZ);
        if (biome != null) cir.setReturnValue(biome);
    }

    @Inject(at = @At("TAIL"), method = "getNoiseBiomeAtQuart")
    public final void setValue(int pX, int pY, int pZ, CallbackInfoReturnable<Holder<Biome>> cir) {
        data = new BiomeCacheData(
                QuartPos.toSection(pX),
                QuartPos.toSection(pY),
                QuartPos.toSection(pZ),
                new WeakReference<>(cir.getReturnValue())
        );
    }
}
