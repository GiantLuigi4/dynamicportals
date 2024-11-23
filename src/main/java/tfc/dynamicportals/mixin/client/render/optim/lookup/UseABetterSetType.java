package tfc.dynamicportals.mixin.client.render.optim.lookup;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.network.util.optim.IndexedArraySet;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class UseABetterSetType {
    @Mutable
    @Shadow @Final
    Set<RenderType> f_112749_;

    @Inject(at = @At("TAIL"), method = "<init>")
    public final void postInit(CallbackInfo ci) {
        // for some reason, mojang uses an array set for this
        // considering mojang does a lot of contains checking and not much adding/removing, a hashset seems more suited
        // probably wanna look more into this later on
        int maxHash = 0;
        for (RenderType type : RenderType.chunkBufferLayers())
            maxHash = Math.max(type.hashCode(), maxHash);
        this.f_112749_ = new IndexedArraySet<>(maxHash + 1);
    }
}
