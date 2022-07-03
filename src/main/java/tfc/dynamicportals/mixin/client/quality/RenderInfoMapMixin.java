package tfc.dynamicportals.mixin.client.quality;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.opt.VecMap;

@Mixin(LevelRenderer.RenderInfoMap.class)
public class RenderInfoMapMixin {
	VecMap<LevelRenderer.RenderChunkInfo> map = new VecMap<>(2);
	
	@Inject(at = @At("HEAD"), method = "put", cancellable = true)
	public void preGet(ChunkRenderDispatcher.RenderChunk pRenderChunk, LevelRenderer.RenderChunkInfo pInfo, CallbackInfo ci) {
		Vec3i vec = pRenderChunk.getOrigin();
		vec = new Vec3i(vec.getX() / 16, vec.getY() / 16, vec.getZ() / 16);
		map.put(vec, pInfo);
		ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "get", cancellable = true)
	public void preGet(ChunkRenderDispatcher.RenderChunk pRenderChunk, CallbackInfoReturnable<LevelRenderer.RenderChunkInfo> cir) {
		Vec3i vec = pRenderChunk.getOrigin();
		vec = new Vec3i(vec.getX() / 16, vec.getY() / 16, vec.getZ() / 16);
		cir.setReturnValue(map.get(vec));
	}
}
