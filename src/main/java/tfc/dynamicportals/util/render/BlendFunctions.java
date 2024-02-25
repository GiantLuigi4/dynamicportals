package tfc.dynamicportals.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public class BlendFunctions {
	public static void alphaBlend() {
		RenderSystem.defaultBlendFunc();
	}
	
	public static void multiplyBlend() {
		RenderSystem.blendFunc(
				GL11.GL_DST_COLOR,
				GL11.GL_ZERO
		);
	}
	
	public static void additiveBlend() {
		RenderSystem.blendFunc(
				GL11.GL_ONE,
				GL11.GL_ONE
		);
	}
	
	public static void subtractiveBlend() {
		RenderSystem.blendFunc(
				GL11.GL_ZERO,
				GL11.GL_ONE_MINUS_SRC_COLOR
		);
	}
}
