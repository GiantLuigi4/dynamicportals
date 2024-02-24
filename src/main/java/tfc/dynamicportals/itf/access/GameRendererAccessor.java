package tfc.dynamicportals.itf.access;

import net.minecraft.client.Camera;

public interface GameRendererAccessor {
	double dynamic_portals$getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);
}
