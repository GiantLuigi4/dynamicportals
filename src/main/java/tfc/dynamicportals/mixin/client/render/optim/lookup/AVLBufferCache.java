package tfc.dynamicportals.mixin.client.render.optim.lookup;

import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class AVLBufferCache {
    @Mutable
    @Shadow
    @Final
    private Map<RenderType, VertexBuffer> f_112790_;

    @Inject(at = @At("TAIL"), method = "<init>")
    public final void postInit(ChunkRenderDispatcher this$0, int p_202436_, int p_202437_, int p_202438_, int p_202439_, CallbackInfo ci) {
        this.f_112790_ = new Object2ObjectAVLTreeMap<>(Comparator.comparingInt(Object::hashCode));
        for (RenderType type : RenderType.chunkBufferLayers()) {
            f_112790_.put(
                    type,
                    new VertexBuffer(VertexBuffer.Usage.STATIC)
            );
        }
    }
}
