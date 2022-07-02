package tfc.dynamicportals.mixin.client.optimization;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.dynamicportals.mixin.client.access.VertexFormatAccessor;

@Mixin(VertexBuffer.class)
public class VBOMixin {
	@Redirect(method = "drawChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;setupBufferState()V"))
	public void directCall(VertexFormat instance) {
		((VertexFormatAccessor) instance).directSetRenderState();
	}
}
