package tfc.dynamicportals.mixin.client.core;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Renderer;

@Mixin(Window.class)
public class WindowMixin {
	@Shadow
	private int framebufferWidth;
	
	@Shadow
	private int framebufferHeight;
	
	// when the vanilla frame buffer resizes, all dyn portals fbos must resize as well
	@Inject(at = @At("TAIL"), method = "onFramebufferResize")
	public void refreshFramebufferSize(CallbackInfo ci) {
		Renderer.refreshStencilBuffer(framebufferWidth, framebufferHeight);
	}
}
