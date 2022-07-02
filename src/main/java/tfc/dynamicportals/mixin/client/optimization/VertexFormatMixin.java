package tfc.dynamicportals.mixin.client.optimization;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Renderer;

@Mixin(VertexFormat.class)
public class VertexFormatMixin {
	private static VertexFormat bound = null;
	
	// TODO: for some reason, vbos
	@Inject(at = @At("HEAD"), method = "_setupBufferState", cancellable = true)
	public void preSetupBufferState(CallbackInfo ci) {
		if (Renderer.isStencilPresent()) {
			if (bound == (Object) this) ci.cancel();
			bound = (VertexFormat) (Object) this;
		}
	}
	
	@Inject(at = @At("HEAD"), method = "_clearBufferState")
	public void preClearState(CallbackInfo ci) {
		if (Renderer.isStencilPresent()) {
			bound = null;
		}
	}
}
