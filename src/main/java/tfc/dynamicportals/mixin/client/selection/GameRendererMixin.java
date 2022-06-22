package tfc.dynamicportals.mixin.client.selection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.RaytraceHelper;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(at = @At("TAIL"), method = "pick")
	public void postPick(float pPartialTicks, CallbackInfo ci) {
		RaytraceHelper.trace(minecraft, pPartialTicks);
	}
}
