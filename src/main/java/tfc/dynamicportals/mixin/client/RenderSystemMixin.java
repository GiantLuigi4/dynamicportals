package tfc.dynamicportals.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.api.Renderer;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Inject(at = @At("HEAD"), method = "clear", cancellable = true)
	private static void onClear(int pMask, boolean pCheckError, CallbackInfo ci) {
		if (Renderer.isStencilPresent()) {
			if (GLUtils.clear(pMask)) {
				ci.cancel(); // TODO: do this properly, or smth
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "clearColor")
	private static void setClearColor(float pRed, float pGreen, float pBlue, float pAlpha, CallbackInfo ci) {
		GLUtils.setClearColor(pRed, pGreen, pBlue, pAlpha);
	}
}
