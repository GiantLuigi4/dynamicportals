package tfc.dynamicportals.mixin.client.fixes;

import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.access.IClosable;

// Luigi's TODO: the fact that I need this is dumb
@Mixin(VertexBuffer.class)
public class VBOMixin implements IClosable {
	@Unique
	boolean isClosed;
	
	@Inject(at = @At("HEAD"), method = "close")
	public void preClose(CallbackInfo ci) {
		isClosed = true;
	}
	
	@Inject(at = @At("HEAD"), method = "draw", cancellable = true)
	public void preDraw0(CallbackInfo ci) {
		if (isClosed) ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "drawChunkLayer", cancellable = true)
	public void preDraw1(CallbackInfo ci) {
		if (isClosed) ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "_drawWithShader", cancellable = true)
	public void preDraw2(CallbackInfo ci) {
		if (isClosed) ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "upload_", cancellable = true)
	public void preUpload(CallbackInfo ci) {
		if (isClosed) ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "bind", cancellable = true)
	public void preBind(CallbackInfo ci) {
		if (isClosed) ci.cancel();
	}
	
	@Override
	public boolean isClosed() {
		return isClosed;
	}
}
