package tfc.dynamicportals.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.GLUtils;

@Mixin(RenderTarget.class)
public class RenderTargetMixin {
	@Inject(at = @At("HEAD"), method = "bindWrite")
	public void preBindWrite(boolean pSetViewport, CallbackInfo ci) {
		GLUtils.setBound((RenderTarget) (Object) this);
	}
}
