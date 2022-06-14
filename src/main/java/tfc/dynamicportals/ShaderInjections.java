package tfc.dynamicportals;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL13;
import tfc.dynamicportals.api.Renderer;

public class ShaderInjections {
	public static String headInjection() {
		return
				"\n\t/* Dynamic Portals injection */\n" +
						"\tif (float(dynamicPortalsHasStencilTextureSet) > 0.5f) { // gotta love glsl, yk?\n" +
						"\t\tvec2 dynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tvec4 dynamicPortalsColor = texture2D(dynamicPortalsStencilTexture, dynamicPortalsPos);\n" +
						"\t\tif (dynamicPortalsColor.r <= 0.00390625) {\n" +
						"\t\t\tdiscard;\n" +
						"\t\t\treturn;\n" +
						"\t\t}\n" +
						"\t\tvec4 dynamicPortalsDepth = texture2D(dynamicPortalsStencilDepth, dynamicPortalsPos);\n" +
//						"\t\tfragColor = vec4(dynamicPortalsDepth.rrr, 1);\n" +
//						"\t\tfragColor = vec4(gl_FragDepth, gl_FragDepth, gl_FragDepth, 1);\n" +
//						"\t\tfragColor = vec4(gl_FragCoord.zzz, 1);\n" +
						"\t\tif (dynamicPortalsDepth.r > gl_FragCoord.z) {\n" +
						"\t\t\tdiscard;\n" +
						"\t\t\treturn;\n" +
						"\t\t}\n" +
						"\t}\n" +
						"\t/* end Dynamic Portals injection */";
	}
	
	public static void setupTextures(AbstractUniform STENCIL_TEXTURE, AbstractUniform STENCIL_DEPTH) {
		RenderSystem.activeTexture(GL13.GL_TEXTURE10);
		RenderSystem.enableTexture();
		RenderSystem.bindTexture(Renderer.getStencilTexture());
		STENCIL_TEXTURE.set(10);
		if (STENCIL_TEXTURE instanceof Uniform) ((Uniform) STENCIL_TEXTURE).upload();
		RenderSystem.activeTexture(GL13.GL_TEXTURE11);
		RenderSystem.bindTexture(Renderer.getStencilDepth());
		RenderSystem.enableTexture();
		STENCIL_DEPTH.set(11);
		if (STENCIL_DEPTH instanceof Uniform) ((Uniform) STENCIL_DEPTH).upload();
	}
}
