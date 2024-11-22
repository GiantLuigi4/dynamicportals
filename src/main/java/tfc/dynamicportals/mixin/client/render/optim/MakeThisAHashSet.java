package tfc.dynamicportals.mixin.client.render.optim;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class MakeThisAHashSet {
    @Mutable
    @Shadow @Final
    Set<RenderType> f_112749_;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(CallbackInfo ci) {
        // for some reason, mojang uses an array set for this
        // considering mojang does a lot of contains checking and not much adding/removing, a hashset seems more suited
        // probably wanna look more into this later on
        this.f_112749_ = new HashSet<>(12);
    }
}
