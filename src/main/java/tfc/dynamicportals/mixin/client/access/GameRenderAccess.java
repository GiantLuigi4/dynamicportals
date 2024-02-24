package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.itf.access.GameRendererAccessor;

@Mixin(GameRenderer.class)
public abstract class GameRenderAccess implements GameRendererAccessor {
	@Shadow protected abstract double getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);
	
	@Override
	public double dynamic_portals$getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting) {
		return getFov(pActiveRenderInfo, pPartialTicks, pUseFOVSetting);
	}
}
