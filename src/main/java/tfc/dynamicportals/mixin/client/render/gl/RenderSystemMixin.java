package tfc.dynamicportals.mixin.client.render.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.util.RenderUtil;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Inject(at = @At("HEAD"), method = "clear", cancellable = true)
	private static void preClear(int pMask, boolean pCheckError, CallbackInfo ci) {
		// TODO: this does need to be implemented but I'm not yet sure as to how
		if (RenderUtil.activeLayer > 0) {
			ci.cancel();
		}
	}
}
