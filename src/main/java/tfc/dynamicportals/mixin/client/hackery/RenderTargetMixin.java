package tfc.dynamicportals.mixin.client.hackery;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.Renderer;

@Mixin(RenderTarget.class)
public class RenderTargetMixin {
	@Inject(at = @At("HEAD"), method = "bindWrite", cancellable = true)
	public void preBindWrite(boolean pSetViewport, CallbackInfo ci) {
		GLUtils.setBound((RenderTarget) (Object) this);
		
		if (((Object) this) instanceof MainTarget) {
			if (Renderer.bindPortalFBO(pSetViewport)) {
				ci.cancel();
			}
		}
	}
}
