package tfc.dynamicportals;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL13;

public class ShaderInjections {
	public static String tailVertex() {
		return "\n\t/* Dynamic Portals injection */\n" +
				       "if (float(dynamicPortalsHasStencilTextureSet) > 1.5f) {\n" +
				       // Luigi's TODO: get this working
//				"\t\tif (gl_Position.z < 0.01) {\n" +
//				"\t\t\tgl_Position.z = 0.01;\n" +
//				"\t\t}\n" +
				       "\t}\n" +
				       "\t/* end Dynamic Portals injection */\n";
	}
	
	// about iris/oculus/OF
	/*
		// gl_FragData[0] // seems to be the color component of the pixel
		// (assumption) gl_FragCoord // plain glsl builtin, representing the point on screen
		// (assumption) gl_FragDepth // plain glsl builtin, representing the depth of the pixel
		// vertexDistance // ??
	 */
	public static String headInjection(boolean hasTexCoord, String samplerName, boolean hasColorAttrib) {
		// Luigi's TODO: checking of stuff, this should only really be done for the POSITION_TEX shader
		// lorenzo: LUIGI WHY ARE YOU CALLING VARIABLES "yes1" AND "yes" WHAT
		String yes1 = "";
		if (hasColorAttrib) {
			yes1 = " * vertexColor";
		}
		String yes =
				"\t\tdynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tdynamicPortalsColor = texture(" + samplerName + ", dynamicPortalsPos);\n" +
//						"\t\tfragColor = vec4(dynamicPortalsPos, 0, 1);\n" +
						"\t\tfragColor = dynamicPortalsColor" + yes1 + ";\n" +
						"\t\treturn;\n";
		if (!hasTexCoord) yes = "";
		return
				// TODO: something needs to change when sodium is present
				"\n\t/* Dynamic Portals injection */\n" +
						"\tvec2 dynamicPortalsPos;\n" +
						"\tvec4 dynamicPortalsColor;\n" +
						"\tvec4 dynamicPortalsDepth;\n" +
//						"\tfloat dynamicPortalsRoundingVar0;\n" +
//						"\tfloat dynamicPortalsRoundingVar1;\n" +
						"\tif (float(dynamicPortalsHasStencilTextureSet) > 1.5f) { // gotta love glsl, yk?\n" +
						yes +
						"\t} else if (float(dynamicPortalsHasStencilTextureSet) > 0.5f) {\n" +
						"\t\tdynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tdynamicPortalsColor = texture(dynamicPortalsStencilTexture, dynamicPortalsPos);\n" +
						"\t\tif (dynamicPortalsColor.a <= 0.00390625) {\n" +
						"\t\t\tdiscard;\n" +
						"\t\t\treturn;\n" +
						"\t\t}\n" +
						"\t\tdynamicPortalsDepth = texture(dynamicPortalsStencilDepth, dynamicPortalsPos);\n" +
						// TODO: figure out how to make a more lenient depth test
//						"\t\tdynamicPortalsRoundingVar0 = dynamicPortalsDepth.r;\n" +
//						"\t\tdynamicPortalsRoundingVar1 = gl_FragCoord.z;\n" +
//						"\t\tdynamicPortalsRoundingVar0 *= 1000.;\n" +
//						"\t\tdynamicPortalsRoundingVar1 *= 1000.;\n" +
//						"\t\tdynamicPortalsRoundingVar0 = floor(dynamicPortalsRoundingVar0);\n" +
//						"\t\tdynamicPortalsRoundingVar1 = floor(dynamicPortalsRoundingVar1);\n" +
//						"\t\tif (dynamicPortalsRoundingVar0 > dynamicPortalsRoundingVar1) {\n" +
						"\t\tif (dynamicPortalsDepth.r > gl_FragCoord.z) {\n" +
						"\t\t\tdiscard;\n" +
						"\t\t\treturn;\n" +
						"\t\t}\n" +
						"\t}\n" +
						"\t/* end Dynamic Portals injection */";
	}
	
	public static String tailInjection() {
		return """
				
				\t/* Dynamic Portals injection */
				\tif (float(dynamicPortalsHasStencilTextureSet) > 0.5f) {
				\t\tdynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);
				\t\tdynamicPortalsColor = texture(dynamicPortalsStencilTexture, dynamicPortalsPos);
				\t\tfragColor = fragColor * dynamicPortalsColor;
				\t}
				\t/* end Dynamic Portals injection */
				""";
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
