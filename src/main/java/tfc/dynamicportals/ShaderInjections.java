package tfc.dynamicportals;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL13;

public class ShaderInjections {
	public static String tailVertex(boolean hasViewMat, boolean hasProjMat, boolean isBlock) {
		String str =
				"""
						
						\t/* Dynamic Portals injection */
						\tvec4 dynamic_portals_pos = %matrixMath% gl_Position;
						\tdynamicPortalsWorldPos = dynamic_portals_pos.xyzw;
						\t/* end Dynamic Portals injection */
						""";
		// thanks to Khlorghaal
		// TODO: entities are a special butterfly, and need to be treated as such
		if (hasProjMat && hasViewMat) str = str.replace("%matrixMath%", "inverse(ProjMat * ModelViewMat) * %matrixMath%");
		else if (hasProjMat) str = str.replace("%matrixMath%", "inverse(ProjMat) %matrixMath% * ");
		else if (hasViewMat) str = str.replace("%matrixMath%", "inverse(ModelViewMat) * %matrixMath%");
		return str.replace(" %matrixMath%", "");
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
		// luigi: didn't know what to call them
		String colorModulate = hasColorAttrib ? " * vertexColor" : "";
		String worldSpace = hasTexCoord ?
				"\t\tdynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tdynamicPortalsColor = texture(" + samplerName + ", dynamicPortalsPos);\n" +
						"\t\tfragColor = dynamicPortalsColor" + colorModulate + ";\n" +
						"\t\treturn;\n" : "";
		return
				// Luigi's TODO: something needs to change when sodium is present
				"\n\t/* Dynamic Portals injection */\n" +
						"\tvec2 dynamicPortalsPos;\n" +
						"\tvec4 dynamicPortalsColor;\n" +
						"\tvec4 dynamicPortalsDepth;\n" +
						"\tif (float(dynamicPortalsHasStencilTextureSet) > 1.5f) { // gotta love glsl, yk?\n" +
						worldSpace +
						"\t} else if (float(dynamicPortalsHasStencilTextureSet) > 0.5f) {\n" +
						"\t\tdynamicPortalsPos = gl_FragCoord.xy / (dynamicPortalsFBOSize * 1.);\n" +
						"\t\tdynamicPortalsColor = texture(dynamicPortalsStencilTexture, dynamicPortalsPos);\n" +
						"\t\tif (dynamicPortalsColor.a <= 0.00390625) {\n" +
						"\t\t\tdiscard;\n" +
						"\t\t\treturn;\n" +
						"\t\t}\n" +
						//I want to debug stuff, but I am unable to do so
						"\t\tdynamicPortalsDepth = texture(dynamicPortalsStencilDepth, dynamicPortalsPos);\n" + //texture(dynamicPortalsStencilDepth, dynamicPortalsPos);
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
				\t\tdynamicPortalsDepth = texture(dynamicPortalsStencilDepth, dynamicPortalsPos);
				\t\tif (dynamicPortalsDepth.r < 2) {
				\t\t\tfloat v = 1 - (dynamicPortalsDepth.r * 0.5);
				\t\t\tv *= v;
				\t\t\tdynamicPortalsColor = mix(dynamicPortalsColor, vec4(1), v);
				\t\t}
				\t\tfragColor *= dynamicPortalsColor;
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
	
	public static String getMethods() {
		return """
				// https://github.com/glslify/glsl-transpose/blob/master/index.glsl
				mat4 dynamic_portals_transpose(mat4 m) {
					return mat4(m[0][0], m[1][0], m[2][0], m[3][0],
								m[0][1], m[1][1], m[2][1], m[3][1],
								m[0][2], m[1][2], m[2][2], m[3][2],
								m[0][3], m[1][3], m[2][3], m[3][3]);
				}
				""";
	}
}
