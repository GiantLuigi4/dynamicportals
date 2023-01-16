package tfc.dynamicportals.util.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

public class GlStateFunctions {
	public static void enableDepthClamp() {
		GL11.glEnable(GL40.GL_DEPTH_CLAMP);
	}
	
	public static void disableDepthClamp() {
		GL11.glDisable(GL40.GL_DEPTH_CLAMP);
	}
}
