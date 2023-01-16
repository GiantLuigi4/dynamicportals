package tfc.dynamicportals.mixin.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class NoFog {
	
	@Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V", remap = false, at = @At("TAIL"))
	private static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float farDistance, boolean near, float pt, CallbackInfo ci) {
		RenderSystem.setShaderFogShape(FogShape.CYLINDER);
		RenderSystem.setShaderFogStart(10000);
		RenderSystem.setShaderFogEnd(20000);
	}
	
}
