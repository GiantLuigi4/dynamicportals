package tfc.dynamicportals.mixin.client.optimization;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.mixin.client.access.VertexFormatAccessor;
import tfc.dynamicportals.opt.StateReduction;

@Mixin(VertexBuffer.class)
public class VBOMixin {
	@Unique
	boolean hasBeenDrawn = false;
	
	@Redirect(method = "drawChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;setupBufferState()V"))
	public void directCall(VertexFormat instance) {
		// prevent draw skipping if the VBO has not yet been drawn
		StateReduction.maySkipFormatSetup = hasBeenDrawn;
		// reduce overhead a tiny amount
		((VertexFormatAccessor) instance).directSetRenderState();
		hasBeenDrawn = true;
	}
	
	@Inject(at = @At("HEAD"), method = "upload_")
	public void preUpload(BufferBuilder pBuilder, CallbackInfo ci) {
		hasBeenDrawn = false;
	}
}
