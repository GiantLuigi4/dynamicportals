package tfc.dynamicportals.util.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import tfc.dynamicportals.itf.access.GameRendererAccessor;

public class RenderUtil {
	public static Matrix4f getProjectionMatrix(Camera pCamera, float pPartialTick, GameRenderer pGameRenderer, Minecraft mc) {
		double d0 = ((GameRendererAccessor) pGameRenderer).dynamic_portals$getFov(pCamera, pPartialTick, true);
		return pGameRenderer.getProjectionMatrix(Math.max(d0, mc.options.fov().get().intValue()));
	}
	
	public static int activeLayer;
}
