package tfc.dynamicportals;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL13;

public class ShaderInjections {
	public static String headInjection(boolean hasTexCoord, String samplerName) {
		// TODO: checking of stuff
		// TODO: this should only really be done for the POSITION_TEX shader
		String yes =
				"\t\tvec2 dynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tvec4 dynamicPortalsColor = texture(" + samplerName + ", dynamicPortalsPos);\n" +
						"\t\tfragColor = dynamicPortalsColor;\n" +
						"\t\treturn;\n";
		if (!hasTexCoord) yes = "";
		return
				"\n\t/* Dynamic Portals injection */\n" +
						"\tif (float(dynamicPortalsHasStencilTextureSet) > 1.5f) { // gotta love glsl, yk?\n" +
						yes +
						"\t} else if (float(dynamicPortalsHasStencilTextureSet) > 0.5f) {\n" +
						"\t\tvec2 dynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tvec4 dynamicPortalsColor = texture2D(dynamicPortalsStencilTexture, dynamicPortalsPos);\n" +
						"\t\tif (dynamicPortalsColor.a <= 0.00390625) {\n" +
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
	
	public static String tailInjection() {
		String str =
				"\n\t/* Dynamic Portals injection */\n" +
				"\tif (float(dynamicPortalsHasStencilTextureSet) > 0.5f) {\n" +
				"\t\tvec2 dynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
				"\t\tvec4 dynamicPortalsColor = texture2D(dynamicPortalsStencilTexture, dynamicPortalsPos);\n" +
				"\t\tfragColor = fragColor * dynamicPortalsColor;\n" +
				"\t}\n" +
				"\t/* end Dynamic Portals injection */\n";
		return str;
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
